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

                .csrf(csrf -> csrf.disable())

                // 기본적으로 모든 요청은 인증 없이 접근 가능
                .authorizeHttpRequests(auth -> auth
                        // ✅ 로그인, 회원가입, 정적 리소스, 소셜 로그인 등 허용
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/bus", "/oauth2/**").permitAll()

                        // ✅ Swagger 문서 관련 경로는 누구나 접근 가능
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v3/api-docs/**").permitAll()

                        // ✅ 실제 API 요청에 대해서만 인증 필요, 역할 기반 접근 제한
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()  // GET 요청은 인증만 필요
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")  // POST 요청은 ADMIN만 가능
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")  // DELETE 요청은 ADMIN만 가능

                        // ✅ 그 외 나머지 URL은 모두 허용
                        .anyRequest().permitAll()
                )

                // ✅ 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customLoginSuccessHandler)
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

                // ✅ OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();
                            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/login?error=" + encodedMessage);
                        })
                )

                // ✅ 자동 로그인 (Remember Me)
                .rememberMe(remember -> remember
                        .key("remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                )

                // ✅ 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        // ✅ API 키 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
