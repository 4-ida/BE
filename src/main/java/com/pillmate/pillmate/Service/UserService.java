package com.pillmate.pillmate.Service;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // 회원가입
    @Transactional
    public User signUp(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }
        
        // 비밀번호 일치 체크
        if (!request.isPasswordMatch()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        
        // 사용자 생성
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password("") // 임시로 빈 문자열, 아래에서 암호화
                .termsOfService(request.getConsent().getTermsOfService())
                .privacyPolicy(request.getConsent().getPrivacyPolicy())
                .dataUsage(request.getConsent().getDataUsage())
                .provider(AuthProvider.LOCAL)
                .providerId(request.getEmail())
                .build();
        
        // 비밀번호 암호화
        user.encodePassword(request.getPassword());
        
        // 저장
        return userRepository.save(user);
    }
    
    public boolean checkEmailAvailability(String email) {
        String normalized = email == null ? null : email.trim();
        if (normalized == null || normalized.isBlank() || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일 형식입니다.");
        }
        if (userRepository.existsByEmail(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        return true;
    }
    
    // 로그인
    @Transactional
    public LoginResult login(String email, String password) {
        User user = findByEmail(email);
        
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
        return userRepository.findByEmail(email)
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
