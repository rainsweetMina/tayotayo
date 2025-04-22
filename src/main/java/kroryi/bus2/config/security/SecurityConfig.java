package kroryi.bus2.config.security;

import kroryi.bus2.filter.ApiKeyAuthenticationFilter;
import kroryi.bus2.handler.CustomLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService;
    private final UserDetailsService userDetailsService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화

                // 기본적으로 모든 요청은 인증 없이 접근 가능
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/bus", "/oauth2/**").permitAll()  // 로그인, 회원가입, 정적 리소스, 소셜 로그인 허용
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v3/api-docs/**").permitAll()  // Swagger UI 허용
                        .anyRequest().permitAll()  // 그 외 모든 요청 허용
                )

                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customLoginSuccessHandler)  // 로그인 성공 처리
                        .failureHandler((request, response, exception) -> {
                            String errorCode = "error";
                            if (exception instanceof BadCredentialsException) {
                                errorCode = "bad_credentials";  // 잘못된 자격 증명
                            } else if (exception instanceof DisabledException) {
                                errorCode = "disabled";  // 비활성화된 계정
                            } else if (exception instanceof LockedException) {
                                errorCode = "locked";  // 잠긴 계정
                            } else if (exception instanceof AccountExpiredException) {
                                errorCode = "expired";  // 만료된 계정
                            }
                            response.sendRedirect("/login?errorCode=" + errorCode);  // 로그인 실패시 에러 코드 전달
                        })
                        .permitAll()  // 로그인 페이지는 모두 접근 가능
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)  // OAuth2 사용자 정보 처리
                        )
                        .successHandler(customLoginSuccessHandler)  // 로그인 성공 처리
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();
                            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/login?error=" + encodedMessage);  // OAuth2 로그인 실패시 에러 메시지 전달
                        })
                )

                // 자동 로그인 설정 (Remember Me)
                .rememberMe(remember -> remember
                        .key("remember-me-key")  // Remember Me 토큰 키
                        .tokenValiditySeconds(7 * 24 * 60 * 60)  // 7일 동안 유효
                        .rememberMeParameter("remember-me")  // Remember Me 파라미터 이름
                        .userDetailsService(userDetailsService)  // 사용자 세부 정보 서비스 설정
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)  // 세션 무효화
                        .clearAuthentication(true)  // 인증 정보 삭제
                        .deleteCookies("JSESSIONID")  // 쿠키 삭제
                        .logoutSuccessUrl("/login?logout")  // 로그아웃 후 리다이렉트 URL
                        .permitAll()  // 로그아웃 페이지는 모두 접근 가능
                );

        // API 키 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();  // 설정된 필터 체인 반환
    }
}
