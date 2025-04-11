package kroryi.bus2.config.security;


import kroryi.bus2.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll() // 이제는 인증 필요로 변경
                        .requestMatchers("/**").permitAll() // 이제는 인증 필요로 변경
                        .requestMatchers(
                                "/js/**", "/css/**", "/images/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/index.html","/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs/swagger-config", "/swagger-ui.html"
                        ).permitAll()
//                        .anyRequest().authenticated()
                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/admin", true)
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login")
//                )
//                .headers(headers -> headers
//                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // or disable()
//                )
        ;
//                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class); // ✅ 이게 핵심!!

        return http.build();
    }

    // 관리자 계정 예시
//    @Bean
//    public UserDetailsService users() {
//        UserDetails admin = User.withDefaultPasswordEncoder()
//                .username("admin")
//                .password("1234")
//                .roles("ADMIN")
//                .build();
//        return new InMemoryUserDetailsManager(admin);
//    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}