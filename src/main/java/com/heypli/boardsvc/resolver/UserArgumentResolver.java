package com.heypli.boardsvc.resolver;

import com.heypli.boardsvc.annotation.SocialUser;
import com.heypli.boardsvc.domain.User;
import com.heypli.boardsvc.domain.enums.SocialType;
import com.heypli.boardsvc.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//HandlerMethodArgumentResolver는 전략패턴의 일종으로
// 컨트롤러 메서드에 특정조건에 해당하는 파라미터가 있으면 생성한 로직 처리 후
// 해당파라미터에 바인딩해주는 전략 인터페이스
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private UserRepository userRepository;

    public UserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 해당 파라미터를 지원할지 여부를 반환
    // true이면 resolveArgument메서드가 수행됨
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @SocialUser어노테이션이 있고 타입이 User인 파라미터만 true를 반환
        // 처음 한 번 체크된 부분은 캐시되어 동일 호출은 체크하지 않고 결과값을 바로 반환
        return parameter.getParameterAnnotation(SocialUser.class) != null &&
                parameter.getParameterType().equals(User.class);
    }

    // 파라미터 인잣값에 대한 정보를 바탕으로 실제 객체를 생성해서 해당 파라미터에 바인딩
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        User user = (User) session.getAttribute("user");
        return getUser(user, session);
    }

    private User getUser(User user, HttpSession session) { // 인증된 User객체를 만드는 메서드
        if(user == null) {
            try {
                // 액세스 토큰까지 제공한다는 의미에서 Oauth2AuthenticationToken 지원
                // SecurityContextHolder에서 토큰을 가져온다.
                OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                // 개인정보를 getAttributes메서드를 사용해 불러온다.
                Map<String, Object> map = authentication.getPrincipal().getAttributes();
                // getAuthorizedClientRegistrationdId()로 인증된 소셜미디어를 확인할 수 있다.
                User convertUser = convertUser(authentication.getAuthorizedClientRegistrationId(), map);
                user = userRepository.findByEmail(convertUser.getEmail());
                if (user == null) {
                    user = userRepository.save(convertUser);
                }

                setRoleIfNotSave(user, authentication, map);
                session.setAttribute("user", user);
            }catch (ClassCastException e) {
                return user;
            }
        }
        return user;
    }


    private User convertUser(String authority, Map<String, Object> map) { // 사용자의 인증된 소셜미디어타입에 따라 빌더를 이용하여 User객체를 만듬
        if(SocialType.GOOGLE.isEquals(authority)) return getModernUser(SocialType.GOOGLE, map);
        else if(SocialType.KAKAO.isEquals(authority)) return getKaKaoUser(map);
        return null;
    }

    private User getModernUser(SocialType socialType, Map<String, Object> map) {
        return User.builder()
                .name(String.valueOf(map.get("name")))
                .email(String.valueOf(map.get("email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .build();
    }

    private User getKaKaoUser(Map<String, Object> map) {
        HashMap<String, String> propertyMap = (HashMap<String, String>) map.get("properties");
        return User.builder()
                .name(propertyMap.get("nickname"))
                .email(String.valueOf(map.get("kaccount_email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(SocialType.KAKAO)
                .createdDate(LocalDateTime.now())
                .build();
    }

    // 인증된 authentication이 권한을 갖고 있는지 체크
    private void setRoleIfNotSave(User user, OAuth2AuthenticationToken authenticationToken, Map<String, Object> map) {
        if(!authenticationToken.getAuthorities().contains(
                new SimpleGrantedAuthority(user.getSocialType().getRoleType()))) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(map, "N/A",
                            AuthorityUtils.createAuthorityList(user.getSocialType().getRoleType())));
        }
    }
}
