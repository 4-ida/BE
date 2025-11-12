package com.pillmate.pillmate.Service;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pillmate.pillmate.DTO.BasicProfileResponse;
import com.pillmate.pillmate.DTO.BasicProfileUpdateRequest;
import com.pillmate.pillmate.DTO.SignUpRequest;
import com.pillmate.pillmate.DTO.UserProfileResponse;
import com.pillmate.pillmate.DTO.UserProfileUpdateRequest;
import com.pillmate.pillmate.Domain.AuthProvider;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // 회원가입
    @Transactional
    public User signUp(SignUpRequest request) {
        // 이메일 정규화 (trim, 소문자 변환)
        String normalizedEmail = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        
        // 이메일 형식 검증
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }
        
        // 비밀번호 일치 체크
        if (!request.isPasswordMatch()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        
        // 사용자 생성 (약관 동의는 기본값 false)
        User user = User.builder()
                .name(request.getName())
                .email(normalizedEmail)  // 정규화된 이메일 사용
                .password("") // 임시로 빈 문자열, 아래에서 암호화
                .termsOfService(false)
                .privacyPolicy(false)
                .dataUsage(false)
                .provider(AuthProvider.LOCAL)
                .providerId(normalizedEmail)  // providerId도 정규화된 이메일 사용
                .build();
        
        // 비밀번호 암호화
        user.encodePassword(request.getPassword());
        
        // 저장
        return userRepository.save(user);
    }
    
    public boolean checkEmailAvailability(String email) {
        // 이메일 정규화 (trim, 소문자 변환)
        String normalized = email == null ? null : email.trim().toLowerCase();
        log.debug("Checking email availability: original={}, normalized={}", email, normalized);
        
        if (normalized == null || normalized.isBlank() || !EMAIL_PATTERN.matcher(normalized).matches()) {
            log.debug("Email validation failed: normalized={}", normalized);
            return false;
        }
        
        boolean exists = userRepository.existsByEmail(normalized);
        log.debug("Email exists check result: normalized={}, exists={}", normalized, exists);
        
        if (exists) {
            // 디버깅을 위해 실제 DB에 저장된 이메일 확인
            Optional<User> existingUser = userRepository.findByEmail(normalized);
            if (existingUser.isPresent()) {
                log.warn("Email already exists: normalized={}, stored_email={}", normalized, existingUser.get().getEmail());
            }
            return false;
        }
        return true;
    }
    
    // 로그인
    @Transactional
    public LoginResult login(String email, String password) {
        // 이메일 정규화 (trim, 소문자 변환)
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        User user = findByEmail(normalizedEmail);
        
        // 비밀번호 검증
        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        
        boolean firstLogin = user.markLogin();
        return new LoginResult(user, firstLogin);
    }
    
    @Transactional
    public boolean markLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return user.markLogin();
    }
    
    // 이메일로 사용자 찾기
    public User findByEmail(String email) {
        // 이메일 정규화 (trim, 소문자 변환)
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }
    // 프로필 조회
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return UserProfileResponse.from(user);
    }

    // 프로필 수정
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // DTO에 profileImage가 없으니까 여기서는 null 넣어서 그대로 두게 함
        user.updateProfile(
            req.getName(),
            req.getProfileImage(),// 프로필 이미지는 이번 DTO에 없으니까 변경 안 함
            req.getCaffeineSensitivity(),
            req.getAlcoholPattern()  // alcoholPattern을 drinkingPattern으로 매핑
        );

        return UserProfileResponse.from(user);
    }

    // 기본 프로필 조회
    public BasicProfileResponse getBasicProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return BasicProfileResponse.from(user);
    }

    // 기본 프로필 수정
    @Transactional
    public BasicProfileResponse updateBasicProfile(Long userId, BasicProfileUpdateRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.updateBasicProfile(
            req.getDefaultCaffeineAmount(),
            req.getDefaultAlcoholAmount(),
            req.getCurrentMedications(),
            req.getPreferredBeverageType()
        );

        return BasicProfileResponse.from(user);
    }

    @Getter
    @AllArgsConstructor
    public static class LoginResult {
        private final User user;
        private final boolean firstLogin;
    }
}
