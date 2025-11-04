package com.pillmate.pillmate.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pillmate.pillmate.DTO.SignUpRequest;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Repository.UserRepository;
import com.pillmate.pillmate.Util.PasswordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
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
        
        // 비밀번호 형식 검증 (8자 이상 + 영문/숫자/특수문자 조합)
        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다");
        }
        
        // 필수 약관 동의 검증
        if (request.getConsent() == null) {
            throw new IllegalArgumentException("약관 동의 정보는 필수입니다");
        }
        
        if (request.getConsent().getTermsOfService() == null || !request.getConsent().getTermsOfService()) {
            throw new IllegalArgumentException("서비스 이용약관 동의는 필수입니다");
        }
        
        if (request.getConsent().getPrivacyPolicy() == null || !request.getConsent().getPrivacyPolicy()) {
            throw new IllegalArgumentException("개인정보 처리방침 동의는 필수입니다");
        }
        
        // dataUsage는 선택 사항이므로 null 체크만 수행
        Boolean dataUsage = request.getConsent().getDataUsage() != null ? 
            request.getConsent().getDataUsage() : false;
        
        // 사용자 생성
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password("") // 임시로 빈 문자열, 아래에서 암호화
                .termsOfService(request.getConsent().getTermsOfService())
                .privacyPolicy(request.getConsent().getPrivacyPolicy())
                .dataUsage(dataUsage)
                .build();
        
        // 비밀번호 암호화
        user.encodePassword(request.getPassword());
        
        // 저장
        return userRepository.save(user);
    }
    
    // 로그인
    public User login(String email, String password) {
        User user = findByEmail(email);
        
        // 비밀번호 검증
        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        
        return user;
    }
    
    // 이메일로 사용자 찾기
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }
}
