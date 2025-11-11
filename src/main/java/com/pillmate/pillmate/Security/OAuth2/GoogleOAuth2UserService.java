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
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            
            // 디버깅: 구글에서 받은 모든 속성 확인
            Map<String, Object> attributes = oAuth2User.getAttributes();
            log.info("구글 OAuth2 사용자 속성: {}", attributes);
            
            // 구글 사용자 정보 추출
            String email = oAuth2User.getAttribute("email");
            String nameAttribute = oAuth2User.getAttribute("name");
            String providerId = oAuth2User.getAttribute("sub"); // 구글 고유 ID
            
            log.info("추출된 정보 - email: {}, name: {}, providerId: {}", email, nameAttribute, providerId);
            
            if (email == null || providerId == null) {
                log.error("필수 정보 누락 - email: {}, providerId: {}", email, providerId);
                throw new OAuth2AuthenticationException("구글 인증 정보를 가져올 수 없습니다 (email 또는 providerId 누락)");
            }
            
            // 이메일 정규화 (trim, 소문자 변환)
            String normalizedEmail = email.trim().toLowerCase();
            
            // name이 null이면 email 사용
            final String name = (nameAttribute == null || nameAttribute.trim().isEmpty()) 
                    ? normalizedEmail.split("@")[0] 
                    : nameAttribute;
            
            if (nameAttribute == null || nameAttribute.trim().isEmpty()) {
                log.info("name이 null이어서 email에서 추출: {}", name);
            }
            
            final AuthProvider provider = AuthProvider.GOOGLE;
            
            // 디버깅: 조회 전에 값 확인
            log.info("사용자 조회 시도 - provider: {}, providerId: {}", provider, providerId);
            
            // provider와 providerId로 기존 사용자 확인 (소셜 로그인 사용자)
            Optional<User> existingUserOpt = userRepository.findByProviderAndProviderId(provider, providerId);
            log.info("기존 사용자 조회 결과: {}", existingUserOpt.isPresent() ? "찾음" : "없음");
            
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                log.info("기존 사용자 발견 - id: {}, email: {}, provider: {}, providerId: {}", 
                        existingUser.getId(), existingUser.getEmail(), 
                        existingUser.getProvider(), existingUser.getProviderId());
                return new CustomUserDetails(existingUser);
            }
            
            // 기존 사용자가 없으면 이메일로도 확인 (이미 가입된 구글 사용자인지 체크)
            Optional<User> existingUserByEmail = userRepository.findByEmail(normalizedEmail);
            if (existingUserByEmail.isPresent()) {
                User existingEmailUser = existingUserByEmail.get();
                log.info("이메일로 기존 사용자 발견 - id: {}, email: {}, provider: {}, providerId: {}", 
                        existingEmailUser.getId(), existingEmailUser.getEmail(), 
                        existingEmailUser.getProvider(), existingEmailUser.getProviderId());
                
                // 이미 구글 로그인 사용자인 경우
                if (existingEmailUser.getProvider() == AuthProvider.GOOGLE) {
                    log.info("구글 사용자로 인식 - providerId 업데이트 가능성 확인");
                    // providerId가 다른 경우 업데이트 (구글 계정이 변경되었거나 providerId가 바뀐 경우)
                    if (!existingEmailUser.getProviderId().equals(providerId)) {
                        log.warn("providerId 불일치 - 기존: {}, 새로운: {}", 
                                existingEmailUser.getProviderId(), providerId);
                        // providerId 업데이트는 하지 않고, 기존 사용자 반환
                    }
                    return new CustomUserDetails(existingEmailUser);
                } else {
                    // 다른 방식으로 가입된 사용자
                    log.error("이미 다른 방식({})으로 가입된 이메일: {}", existingEmailUser.getProvider(), normalizedEmail);
                    throw new OAuth2AuthenticationException("이미 다른 방식으로 가입된 이메일입니다");
                }
            }
            
            // 새 사용자 생성
            log.info("새 사용자 생성 시작 - email: {}, name: {}", normalizedEmail, name);
            User newUser = User.createSocialUser(normalizedEmail, name, provider, providerId);
            User savedUser = userRepository.save(newUser);
            log.info("새 사용자 생성 완료 - id: {}, email: {}", savedUser.getId(), savedUser.getEmail());
            return new CustomUserDetails(savedUser);
        } catch (OAuth2AuthenticationException e) {
            log.error("OAuth2 인증 오류: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            OAuth2Error oauth2Error = new OAuth2Error("oauth2_error", "소셜 로그인 처리 중 오류가 발생했습니다: " + e.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }
    }
}

