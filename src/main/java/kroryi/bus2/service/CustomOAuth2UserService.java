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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> originalAttributes = oAuth2User.getAttributes();

        // ✅ 수정 가능한 맵으로 복사
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

            attributes.put("id", attributes.get("id")); // 보장

        } else if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
            signupType = SignupType.GOOGLE;

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        final String finalEmail = email;
        final String finalNickname = nickname;
        final SignupType finalSignupType = signupType;
        final String finalUserId = registrationId + "_" + email;

        User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setUsername(finalNickname);
            newUser.setUserId(finalUserId);
            newUser.setPassword("SOCIAL_LOGIN_USER");
            newUser.setRole(Role.USER);
            newUser.setSignupType(finalSignupType);
            newUser.setSignupDate(LocalDate.now());
            return userRepository.save(newUser);
        });

        return new CustomOAuth2User(
                new DefaultOAuth2User(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                        attributes,
                        "id"
                ),
                finalEmail,
                finalNickname,
                finalUserId // ✅ 여기 추가
        );
    }
}
