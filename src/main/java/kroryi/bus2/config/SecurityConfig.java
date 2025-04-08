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
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/bus").permitAll()
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
                .rememberMe(remember -> remember
                        .key("remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일간 자동 로그인 유지
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
