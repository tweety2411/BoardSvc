package com.heypli.boardsvc;

import com.heypli.boardsvc.domain.Board;
import com.heypli.boardsvc.domain.User;
import com.heypli.boardsvc.domain.enums.BoardType;
import com.heypli.boardsvc.repository.BoardRepository;
import com.heypli.boardsvc.repository.UserRepository;
import com.heypli.boardsvc.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootApplication
public class BoardSvcApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(BoardSvcApplication.class, args);
    }

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
    }

    @Bean
    public CommandLineRunner runner(UserRepository userRepository, BoardRepository boardRepository) throws Exception {
        return (args) -> {
            User user = userRepository.save(User.builder()
                    .name("user1")
                    .password("1234")
                    .email("test@naver.com")
                    .createdDate(LocalDateTime.now())
                    .build());

            IntStream.rangeClosed(1, 200)
                    .forEach(idx ->
                            boardRepository.save(Board.builder()
                                    .title("제목" + idx)
                                    .subTitle("idx : " + idx)
                                    .boardType(BoardType.free)
                                    .content("내용" + idx)
                                    .createdDate(LocalDateTime.now())
                                    .updatedDate(LocalDateTime.now())
                                    .user(user)
                                    .build())
                    );

        };
    }

}
