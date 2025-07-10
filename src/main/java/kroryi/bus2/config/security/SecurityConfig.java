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
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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
                                "/css/**", "/js/**", "/images/**", "/img/**", "/static/**", "/public/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/logout",
                                "/register", "/oauth2/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/**",
                                "/webjars/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/health", "/api/cors-test").permitAll()  // 헬스체크 및 CORS 테스트 엔드포인트 추가
                        .requestMatchers("/api/user/check-id").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bus/getRouteInfo").permitAll()
                        .requestMatchers("/api/user/info").hasAnyRole("USER", "ADMIN", "BUS")

                        /* 공개 페이지 */
                        .requestMatchers("/api/schedule", "/schedule", "/low-schedule", "/fare", "/bus-company", "/bus-info").permitAll()

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
                        .requestMatchers("/api/fares/**").permitAll()  // 버스 요금 API 공개 접근 허용
                        .requestMatchers("/api/qna/page", "/api/qna/{id}").permitAll()
                        .requestMatchers("/api/found/**").permitAll()  // 습득물 관련 API 공개 접근 허용
                        .requestMatchers("/api/lost/**").permitAll()  // 분실물 관련 API 공개 접근 허용
                        .requestMatchers("/api/schedule/**").permitAll()  // 버스 시간표 API 공개 접근 허용
                        .requestMatchers("/api/companies", "/api/route-nos", "/api/route-nos-low", "/api/route-notes", "/api/route-id/**", "/api/schedule-header", "/api/route-map", "/api/lowbus-scheduls").permitAll()  // 버스 관련 API 공개 접근 허용

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
        
        // 허용할 오리진 패턴 설정
        config.setAllowedOriginPatterns(List.of(
                "https://*.yi.or.kr:*",
                "http://*.yi.or.kr:*",
                "https://localhost:*",
                "http://localhost:*",
                "https://192.168.*:*",
                "http://192.168.*:*",
                "https://10.*:*",
                "http://10.*:*"
        ));
        
        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 허용할 헤더
        config.setAllowedHeaders(List.of(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // 노출할 헤더
        config.setExposedHeaders(List.of(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "X-Access-Token",
                "X-Refresh-Token"
        ));
        
        // 쿠키 허용 (withCredentials: true와 일치시키기 위해 true로 변경)
        config.setAllowCredentials(true);
        
        // 프리플라이트 요청 캐시 시간
        config.setMaxAge(3600L); // 1시간
        
        log.info("🔧 CORS 설정 완료: allowCredentials={}, allowedOrigins={}", 
                config.getAllowCredentials(), config.getAllowedOriginPatterns());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    // 세션 기반 인증을 사용하지 않으므로 SameSite 쿠키 필터 제거
}
