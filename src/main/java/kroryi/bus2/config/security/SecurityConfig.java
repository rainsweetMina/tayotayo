package kroryi.bus2.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    // ✅ OAuth2 사용자 정보 서비스 주입
    @Autowired
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService;

    // ✅ 비밀번호 암호화 빈 등록 (BCrypt 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 시큐리티 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                // ✅ 사용자 정보 서비스 설정
                .userDetailsService(userDetailsService)

                // ✅ CSRF 보호 비활성화 (개발 시 또는 API 서버에서 사용)
                .csrf(csrf -> csrf.disable())

                // ✅ URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/register", "/css/**", "/js/**", "/bus", "/oauth2/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/mypage/**").authenticated()
                        .anyRequest().permitAll()
                )

                // ✅ 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/mypage", true)
                        .failureHandler((request, response, exception) -> {
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

                // ✅ 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/mypage/", true)
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();
                            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/login?error=" + encodedMessage);
                        })
                )

                // ✅ 자동 로그인(remember-me) 설정
                .rememberMe(remember -> remember
                        .key("remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                )

                // ✅ 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)        // ✅ 세션 무효화
                        .clearAuthentication(true)          // ✅ 인증 정보 삭제
                        .deleteCookies("JSESSIONID")        // ✅ 쿠키 삭제
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
