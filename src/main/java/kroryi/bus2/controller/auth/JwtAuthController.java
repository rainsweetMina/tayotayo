package kroryi.bus2.controller.auth;

import kroryi.bus2.dto.user.JwtTokenDTO;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.service.user.UserService;
import kroryi.bus2.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
public class JwtAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String userId = loginRequest.get("userId");
            String password = loginRequest.get("password");

            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userId, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 사용자 정보 조회
            User user = userService.findByUserId(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "사용자를 찾을 수 없습니다."));
            }

            // JWT 토큰 생성
            String accessToken = jwtTokenUtil.generateAccessToken(user);
            String refreshToken = jwtTokenUtil.generateRefreshToken(user);

            JwtTokenDTO tokenResponse = JwtTokenDTO.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1시간
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok(tokenResponse);

        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshRequest) {
        try {
            String refreshToken = refreshRequest.get("refreshToken");
            
            if (refreshToken == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "리프레시 토큰이 필요합니다."));
            }

            // 리프레시 토큰 검증
            var claims = jwtTokenUtil.parseToken(refreshToken);
            String tokenType = jwtTokenUtil.getTokenType(refreshToken);
            
            if (!"REFRESH".equals(tokenType)) {
                return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 토큰 타입입니다."));
            }

            String userId = claims.getSubject();
            User user = userService.findByUserId(userId);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "사용자를 찾을 수 없습니다."));
            }

            // 새로운 액세스 토큰 생성
            String newAccessToken = jwtTokenUtil.generateAccessToken(user);

            JwtTokenDTO tokenResponse = JwtTokenDTO.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();

            return ResponseEntity.ok(tokenResponse);

        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "토큰 갱신에 실패했습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "유효하지 않은 토큰 형식입니다."));
            }

            String token = authHeader.substring(7);
            var claims = jwtTokenUtil.parseToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", claims.getSubject());
            response.put("email", claims.get("email"));
            response.put("username", claims.get("username"));
            response.put("role", claims.get("role"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "유효하지 않은 토큰입니다."));
        }
    }
} 