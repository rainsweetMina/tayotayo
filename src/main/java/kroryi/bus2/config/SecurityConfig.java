package kroryi.bus2.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Autowired
    private OAuth2UserService<?, ?> customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/bus", "/oauth2/**",
                                "/swagger-ui/**",         // ✅ Swagger UI 경로
                                "/swagger-ui.html",         // ✅ Swagger UI 경로
                                "/v3/api-docs/**",        // ✅ Swagger 문서 JSON
                                "/swagger-resources/**",  // ✅ Swagger 리소스
                                "/webjars/**"             // ✅ Swagger 스타일 등 정적 리소스
                                        ).permitAll()
                        .requestMatchers("/mypage/**").authenticated()
                        .anyRequest().permitAll()
                )
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
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/mypage", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService((OAuth2UserService<OAuth2UserRequest, OAuth2User>) customOAuth2UserService)
                        )
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace(); // 콘솔에 로그

                            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/login?error=" + encodedMessage);
                        })
                )

                .rememberMe(remember -> remember
                        .key("remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }
}
