package kroryi.bus2.config.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.filter.ApiKeyAuthenticationFilter;
import kroryi.bus2.filter.JwtAuthenticationFilter;
import kroryi.bus2.filter.SwaggerAuthFilter;
import kroryi.bus2.handler.CustomLoginSuccessHandler;
import kroryi.bus2.handler.CustomLogoutSuccessHandler;
import kroryi.bus2.handler.CustomOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import kroryi.bus2.utils.JwtTokenUtil;
import kroryi.bus2.service.user.UserService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService;
    private final UserDetailsService userDetailsService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final SwaggerAuthFilter swaggerAuthFilter;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserService userService) {
        return new JwtAuthenticationFilter(jwtTokenUtil, userService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(swaggerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json; charset=UTF-8");
                            String message = "아이디 또는 비밀번호가 올바르지 않습니다.";
                            if (exception instanceof DisabledException) message = "비활성화된 계정입니다.";
                            else if (exception instanceof LockedException) message = "잠긴 계정입니다.";
                            else if (exception instanceof AccountExpiredException) message = "계정이 만료되었습니다.";
                            response.getWriter().write("{\"message\": \"" + message + "\"}");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                )
                .authorizeHttpRequests(auth -> auth
                        // ✅ 정적 리소스
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/logout", "/register", "/oauth2/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/**", "/webjars/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/user/check-id").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bus/getRouteInfo").permitAll()

                        // ✅ 회원가입 허용 (중요!)
                        .requestMatchers(HttpMethod.POST, "/api/user/join").permitAll()

                        // ✅ 이메일 인증 관련 허용
                        .requestMatchers("/api/user/email/send").permitAll()
                        .requestMatchers("/api/user/email/verify").permitAll()

                        // ✅ JWT 인증 엔드포인트 허용
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/validate").permitAll()

                        // ✅ 마이페이지 라우팅 허용 (Vue에서 처리)
                        .requestMatchers("/mypage/**", "/admin/**", "/bus/**").permitAll()

                        // ✅ API 권한 설정 (join보다 아래에 있으면 안 됨!)
                        .requestMatchers("/api/user/apikey/summary").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/mypage/**").hasRole("USER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/user/info").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/logout").permitAll()

                        // ✅ 나머지
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(customOAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String encoded = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("https://localhost:5173/login?error=" + encoded);
                        })
                )
                .rememberMe(remember -> remember
                        .key("remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                        .useSecureCookie(true)
                        .rememberMeCookieName("remember-me")
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://localhost:5173",
                "http://localhost:5173",
                "https://localhost:5174",
                "http://localhost:5174"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    // ✅ SameSite=None; Secure 쿠키 속성 설정 필터
    @Bean
    public FilterRegistrationBean<Filter> sameSiteCookieFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                chain.  doFilter(request, response);

                if (response instanceof HttpServletResponse httpServletResponse) {
                    Collection<String> headers = httpServletResponse.getHeaders(HttpHeaders.SET_COOKIE);
                    boolean firstHeader = true;
                    for (String header : headers) {
                        if (header.contains("JSESSIONID")) {
                            String newHeader = header + "; SameSite=None; Secure";
                            if (firstHeader) {
                                httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, newHeader);
                                firstHeader = false;
                            } else {
                                httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, newHeader);
                            }
                        }
                    }
                }
            }
        });

        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
