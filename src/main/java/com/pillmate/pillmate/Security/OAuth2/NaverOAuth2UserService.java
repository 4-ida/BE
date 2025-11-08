package com.pillmate.pillmate.Security.OAuth2;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pillmate.pillmate.Domain.AuthProvider;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Repository.UserRepository;
import com.pillmate.pillmate.Security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class NaverOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            Map<String, Object> attributes = oAuth2User.getAttributes();
            log.info("네이버 OAuth2 사용자 속성: {}", attributes);

            Map<String, Object> response = extractResponse(attributes);
            String providerId = getString(response.get("id"));
            String email = getString(response.get("email"));
            String nameAttribute = getString(response.get("name"));

            log.info("추출된 네이버 사용자 정보 - providerId: {}, email: {}, name: {}", providerId, email, nameAttribute);

            if (providerId == null) {
                throw new OAuth2AuthenticationException("네이버 인증 정보를 가져올 수 없습니다 (providerId 누락)");
            }

            if (email == null || email.isBlank()) {
                throw new OAuth2AuthenticationException("네이버 계정 이메일 동의가 필요합니다");
            }

            final String name = (nameAttribute == null || nameAttribute.isBlank())
                    ? email.split("@")[0]
                    : nameAttribute;

            final AuthProvider provider = AuthProvider.NAVER;

            Optional<User> existingUserOpt = userRepository.findByProviderAndProviderId(provider, providerId);
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                log.info("기존 네이버 사용자 발견 - id: {}, email: {}", existingUser.getId(), existingUser.getEmail());
                return new CustomUserDetails(existingUser);
            }

            Optional<User> existingUserByEmail = userRepository.findByEmail(email);
            if (existingUserByEmail.isPresent()) {
                User existingEmailUser = existingUserByEmail.get();
                log.info("이메일로 기존 사용자 발견 - id: {}, provider: {}", existingEmailUser.getId(),
                        existingEmailUser.getProvider());

                if (existingEmailUser.getProvider() == AuthProvider.NAVER) {
                    return new CustomUserDetails(existingEmailUser);
                } else {
                    throw new OAuth2AuthenticationException("이미 다른 방식으로 가입된 이메일입니다");
                }
            }

            User newUser = User.createSocialUser(email, name, provider, providerId);
            User savedUser = userRepository.save(newUser);
            log.info("새 네이버 사용자 생성 완료 - id: {}, email: {}", savedUser.getId(), savedUser.getEmail());
            return new CustomUserDetails(savedUser);
        } catch (OAuth2AuthenticationException e) {
            log.error("네이버 OAuth2 인증 오류: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("네이버 OAuth2 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
            OAuth2Error oauth2Error = new OAuth2Error("oauth2_error",
                    "네이버 소셜 로그인 처리 중 오류가 발생했습니다: " + e.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractResponse(Map<String, Object> attributes) {
        Object responseObj = attributes.get("response");
        if (responseObj instanceof Map<?, ?> responseMap) {
            return (Map<String, Object>) responseMap;
        }
        throw new OAuth2AuthenticationException("네이버 사용자 정보(response)가 존재하지 않습니다");
    }

    private String getString(Object value) {
        return value == null ? null : value.toString();
    }
}

