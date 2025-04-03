package kroryi.bus2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/bus", "/login", "/css/**", "/js/**").permitAll() // 로그인 페이지 및 정적 리소스 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .formLogin(login -> login
                        .loginPage("/login") // 로그인 페이지 설정
                        .defaultSuccessUrl("/") // 로그인 성공 시 메인 페이지 이동
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .userDetailsService(userDetailsService); // 🔹 UserDetailsService 설정 추가

        return http.build();
    }
}
