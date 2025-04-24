package kroryi.bus2.config.security;

import kroryi.bus2.filter.ApiKeyAuthenticationFilter;
import kroryi.bus2.filter.SwaggerAuthFilter;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService;
    private final UserDetailsService userDetailsService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final SwaggerAuthFilter swaggerAuthFilter;

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

                // Swagger → API 키 인증 순으로 필터 적용
                .addFilterBefore(swaggerAuthFilter, UsernamePasswordAuthenticationFilter.class)

// -------------------- swagger 보안 (우선제외시킴)
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입, 정적 자원 등 허용
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/bus", "/oauth2/**").permitAll()

                        // Swagger는 인증만 되면 접근 가능
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        // ✅ 마이페이지는 USER 권한만 접근 가능
//                        .requestMatchers("/mypage/**").hasRole("USER")

                        // ✅ 관리자 전용 페이지는 ADMIN 권한만 접근 가능
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/**").permitAll()

                        // 그 외 요청은 모두 인증 필요
                        .anyRequest().authenticated()
                )

                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String errorCode = "error";
                            if (exception instanceof BadCredentialsException) errorCode = "bad_credentials";
                            else if (exception instanceof DisabledException) errorCode = "disabled";
                            else if (exception instanceof LockedException) errorCode = "locked";
                            else if (exception instanceof AccountExpiredException) errorCode = "expired";

                            response.sendRedirect("/login?errorCode=" + errorCode);
                        })
                        .permitAll()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/login?error=" + encodedMessage);
                        })
                )

                // Remember Me
                .rememberMe(remember -> remember
                        .key("remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
