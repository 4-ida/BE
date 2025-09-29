package com.pillmate.pillmate.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pillmate.pillmate.DTO.SignUpRequest;
import com.pillmate.pillmate.Domain.User;
import com.pillmate.pillmate.Repository.UserRepository;

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
        
        // 사용자 생성
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password("") // 임시로 빈 문자열, 아래에서 암호화
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
