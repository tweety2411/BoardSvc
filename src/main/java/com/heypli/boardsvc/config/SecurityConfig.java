package com.heypli.boardsvc.config;

import com.heypli.boardsvc.domain.enums.SocialType;
import com.heypli.boardsvc.oauth.CustomOauth2Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
//웹에서 시큐리티 기능을 사용하겠다는 어노테이션
// 자동설정이 적용된다.
@EnableWebSecurity
// 권한, 요청 등 세부 설정을 위해 WebSecurityConfigurerAdapter를 상속받고
// configure(HttpSecurity http)메소드를 오버라이드하여 시큐리티 설정
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        http.authorizeRequests() // 인증 메커니즘을 요청한 HttpServletRequest기반으로 설정
                // 요청패턴을 리스트 형식으로 설정
                .antMatchers("/", "/oauth2/**","/login/**", "/css/**", "/images/**", "/js/**", "/console/**")
                // 누구나 접근을 허용
                        .permitAll()
                .antMatchers("/kakao").hasAuthority(SocialType.KAKAO.getRoleType())
                // 설정한 요청이의외 리퀘스트는 인증된 사용자만 요청할 수 있음
                .anyRequest().authenticated()
            .and()
                .oauth2Login() // 기본적으로 제공되는 구글, 페이스북 인증 적용
                .defaultSuccessUrl("/loginSuccess")
                .failureUrl("/loginFailure")
            .and()
                // 응답헤더에 대한 설정
                // XFrameOptionsHeaderWriter의 최적화 설정을 허용하지 않음
                    .headers().frameOptions().disable()
            .and()
                    .exceptionHandling()
                    //인증의 진입지점 (인증되지 않은 사용자가 리퀘스트를 요청하면 /login으로 이동  
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            .and()
                    .formLogin()
                    // 로그인 성공시 아래 경로로 포워딩  
                    .successForwardUrl("/board/list")
            .and()
                    .logout()
                    .logoutUrl("/logout")
                    //로그아웃 요청시 삭제될 쿠키값 지정
                    .deleteCookies("JSESSIONID")
                    // 세션무효화 수행 
                    .invalidateHttpSession(true)
            .and()
                    // 첫 번째 인자보다 먼저 시작될 필터를 등록
                    .addFilterBefore(filter, CsrfFilter.class)
                    .csrf().disable();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            // Oauth2ClientProperties에는 구글, 페이스북의 정보가 들어있고 카카오는 따로 등록했기 때문에
            // @Value어노테이션을 이용해 수동으로 불러옴
            OAuth2ClientProperties oAuth2ClientProperties, @Value("${custom.oauth2.kakao.client-id}") String kakaoClientId) {

        List<ClientRegistration> registrations = oAuth2ClientProperties.getRegistration()
                .keySet().stream().map(client -> getRegistration(oAuth2ClientProperties, client))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        registrations.add(getProvider(oAuth2ClientProperties, kakaoClientId));
        return new InMemoryClientRegistrationRepository(registrations);
    }

    // 구글, 페이스북 인증 정보를 빌드시켜줌
    private ClientRegistration getRegistration(OAuth2ClientProperties oAuth2ClientProperties, String client) {
        if("google".equals(client)) {
            OAuth2ClientProperties.Registration registration = oAuth2ClientProperties
                    .getRegistration().get("google");
            return CommonOAuth2Provider.GOOGLE.getBuilder(client)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .build();
        }

        return null;
    }

    private ClientRegistration getProvider(OAuth2ClientProperties oAuth2ClientProperties, String clientId) {
        OAuth2ClientProperties.Provider provider =oAuth2ClientProperties.getProvider().get("kakao");
        return CustomOauth2Provider.KAKAO.getBuilder("kakao", provider)
                .clientId(clientId)
                .build();
    }


}
