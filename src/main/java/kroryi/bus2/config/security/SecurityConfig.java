package kroryi.bus2.config.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import kroryi.bus2.components.RedirectProperties;
import kroryi.bus2.filter.ApiKeyAuthenticationFilter;
import kroryi.bus2.filter.JwtAuthenticationFilter;
import kroryi.bus2.filter.SwaggerAuthFilter;
import kroryi.bus2.handler.*;
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
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final RedirectProperties redirect;

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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self' https://docs.yi.or.kr:15173")))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId())
                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(customAuthenticationEntryPoint)   // 401
                        .accessDeniedHandler(customAccessDeniedHandler))            // 403
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(swaggerAuthFilter,  UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler((request, response, ex) -> { /* 생략 */ })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .deleteCookies("JSESSIONID", "accessToken", "refreshToken", "vue_accessToken", "vue_refreshToken")
                        .invalidateHttpSession(true))

                /* ────────────────────────────────
                 *  🚩 권한·인가 RULES
                 * ──────────────────────────────── */
                .authorizeHttpRequests(auth -> auth

                        /* 정적 리소스·공용 엔드포인트 */
                        .requestMatchers("/", "/index.html", "/favicon.ico",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/logout",
                                "/register", "/oauth2/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/**",
                                "/webjars/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/user/check-id").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bus/getRouteInfo").permitAll()
                        .requestMatchers("/api/user/info").hasAnyRole("USER", "ADMIN", "BUS")

                        /* 회원가입·이메일 인증 */
                        .requestMatchers(HttpMethod.POST, "/api/user/join").permitAll()
                        .requestMatchers("/api/user/email/send", "/api/user/email/verify").permitAll()

                        /* 🔒 마이페이지:  ROLE_USER 전용 */
                        .requestMatchers("/mypage/**").hasRole("USER")
                        .requestMatchers("/api/mypage/**").hasRole("USER")

                        /* BUS & ADMIN 공용 */
                        .requestMatchers("/admin/dashboard").hasAnyRole("ADMIN")
                        .requestMatchers("/admin/found", "/admin/lost").hasAnyRole("ADMIN", "BUS")
                        .requestMatchers("/api/admin/found/match/**").hasRole("BUS") //추가된거
                        .requestMatchers("/api/admin/found/**",
                                "/api/admin/lost/**").hasAnyRole("ADMIN", "BUS")

                        /* ADMIN 전용 */
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        /* JWT 인증 엔드포인트 */
                        .requestMatchers("/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/validate").permitAll()

                        /* 공개 API 엔드포인트 */
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/ad/active", "/api/ad/popup").permitAll()
                        .requestMatchers("/api/bus/**").permitAll()
                        .requestMatchers("/api/qna/page", "/api/qna/{id}").permitAll()
                        .requestMatchers("/api/found/**").permitAll()  // 습득물 관련 API 공개 접근 허용
                        .requestMatchers("/api/companies", "/api/route-nos-low", "/api/route-notes", "/api/route-id/**", "/api/schedule-header", "/api/route-map", "/api/lowbus-scheduls").permitAll()  // 버스 관련 API 공개 접근 허용

                        /* USER·ADMIN 공용(로그인 필요) */
                        .requestMatchers("/api/user/apikey/summary").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                        /* 나머지 API는 인증 필요 */
                        .requestMatchers("/api/**").authenticated()

                        /* 나머지는 인증만 필요 */
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(customOAuth2SuccessHandler)
                        .failureHandler((req, res, ex) -> {
                            String encoded = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
                            res.sendRedirect(redirect.getBaseUrl()+"/login?error=" + encoded);
                        }))
                .rememberMe(remember -> remember
                        .key("remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userDetailsService)
                        .useSecureCookie(true)
                        .rememberMeCookieName("remember-me"));

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "https://*.yi.or.kr:*",
                "http://*.yi.or.kr:*",
                "https://localhost:*",
                "http://localhost:*"
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
