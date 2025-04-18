package kroryi.bus2.config.security;

import kroryi.bus2.handler.CustomLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//@EnableMethodSecurity // ✅ 메서드 수준 권한 체크 활성화
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // ✅ 소셜 로그인 사용자 정보 처리 서비스 주입
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService;

    // ✅ 일반 로그인 사용자 정보 서비스 주입
    private final UserDetailsService userDetailsService;

    // ✅ 로그인 성공 시 사용자 권한에 따라 분기 처리하는 핸들러
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    // ✅ 비밀번호 암호화를 위한 BCryptPasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 스프링 시큐리티 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)

                // ✅ CSRF 보호 비활성화 (개발 시 또는 API 서버에서는 보통 비활성화)
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()) // 👈 iframe 허용
                )

                // ✅ URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                    // ✅ 로그인, 회원가입, 정적 리소스 등은 모두 허용
                    .requestMatchers(
                        "/login", "/register", "/css/**", "/js/**", "/bus", "/oauth2/**",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                        "/swagger-resources/**", "/webjars/**"
                    ).permitAll()

                    // ✅ 관리자 전용 페이지는 ADMIN 권한만 접근 가능
//                    .requestMatchers("/admin/**").hasRole("ADMIN")

                    // ✅ 마이페이지는 USER 권한만 접근 가능
//                    .requestMatchers("/mypage/**").hasRole("USER")

                    // ✅ 그 외는 모두 허용
                    .anyRequest().permitAll()
                )

                // ✅ 폼 로그인 설정
                .formLogin(form -> form
                    .loginPage("/login")                     // 로그인 페이지 경로 지정
                    .loginProcessingUrl("/login")            // 로그인 form 전송 처리 URL
                    .successHandler(customLoginSuccessHandler) // ✅ 로그인 성공 시 사용자 역할에 따라 분기
                    .failureHandler((request, response, exception) -> { // 로그인 실패 핸들러
                        String errorCode = "error";
                        if (exception instanceof BadCredentialsException) {
                            errorCode = "bad_credentials";
                        } else if (exception instanceof DisabledException) {
                            errorCode = "disabled";
                        } else if (exception instanceof LockedException) {
                            errorCode = "locked";
                        } else if (exception instanceof AccountExpiredException) {
                            errorCode = "expired";
                        }
                        response.sendRedirect("/login?errorCode=" + errorCode);
                    })
                    .permitAll()
                )

                // ✅ OAuth2 (소셜 로그인) 설정
                .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login") // 로그인 페이지 경로
                    .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService) // OAuth2 사용자 서비스 설정
                    )
                    .successHandler(customLoginSuccessHandler) // ✅ 소셜 로그인 성공 후 처리
                    .failureHandler((request, response, exception) -> { // 실패 시 메시지 인코딩하여 전달
                        exception.printStackTrace();
                        String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                        response.sendRedirect("/login?error=" + encodedMessage);
                    })
                )

                // ✅ 자동 로그인 (Remember-Me) 설정
                .rememberMe(remember -> remember
                    .key("remember-me-key")                     // 고유 키 설정
                    .tokenValiditySeconds(7 * 24 * 60 * 60)     // 7일 유지
                    .rememberMeParameter("remember-me")         // 파라미터 이름
                    .userDetailsService(userDetailsService)     // 사용자 정보 서비스 설정
                )

                // ✅ 로그아웃 설정
                .logout(logout -> logout
                    .logoutUrl("/logout")                       // 로그아웃 URL
                    .invalidateHttpSession(true)                // 세션 무효화
                    .clearAuthentication(true)                  // 인증 정보 제거
                    .deleteCookies("JSESSIONID")                // 쿠키 제거
                    .logoutSuccessUrl("/login?logout")          // 로그아웃 후 이동 경로
                    .permitAll()
                );


        return http.build();
    }


}
