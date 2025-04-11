package kroryi.bus2.service;

import kroryi.bus2.config.security.CustomOAuth2User;
import kroryi.bus2.entity.user.Role;
import kroryi.bus2.entity.user.SignupType;
import kroryi.bus2.entity.user.User;
import kroryi.bus2.repository.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // ex: google, kakao
        Map<String, Object> originalAttributes = oAuth2User.getAttributes();

        // 수정 가능한 Map으로 복사
        Map<String, Object> attributes = new HashMap<>(originalAttributes);

        String email = null;
        String nickname = null;
        SignupType signupType = null;

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
            signupType = SignupType.KAKAO;

            // "id" 값이 Long으로 반환되므로 이를 String으로 변환
            Long kakaoId = (Long) attributes.get("id");
            attributes.put("id", String.valueOf(kakaoId)); // Long을 String으로 변환
        } else if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
            signupType = SignupType.GOOGLE;

            // "sub"이 구글의 고유 ID이므로 이를 "id"로 맵핑
            String googleId = (String) attributes.get("sub");
            attributes.put("id", googleId); // null 방지
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        // 사용자 저장 또는 조회
        String oauthId = (String) attributes.get("id");
        String userId = registrationId + "_" + oauthId;
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // 소셜 로그인 사용자는 비밀번호가 필요 없으므로 password를 null로 설정
            user = new User();
            user.setEmail(email);
            user.setUsername(nickname);
            user.setUserId(userId);
            user.setPassword("SOCIAL_LOGIN"); // 소셜 로그인 사용자에게 비밀번호 기본값 사용
            user.setRole(Role.USER);
            user.setSignupType(signupType);
            user.setSignupDate(LocalDate.now());
            user = userRepository.save(user);
        }

        // OAuth2User 반환 (ROLE, 속성 포함)
        return new CustomOAuth2User(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(), // 닉네임을 대신 사용
                user.getRole().name(), // 권한 정보만 포함
                attributes // 소셜 로그인 사용자 속성 포함
        );
    }
}
