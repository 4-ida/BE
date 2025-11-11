package com.pillmate.pillmate.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class SignUpRequest {
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 20, message = "이름은 최대 20자까지 입력 가능합니다")
    @Schema(description = "사용자 이름", example = "홍길동", maxLength = 20)
    private String name;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    @Schema(description = "비밀번호 (8자 이상, 영문/숫자/특수문자 조합)", example = "Abcd1234!")
    private String password;
    
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    @Schema(description = "비밀번호 확인", example = "Abcd1234!")
    private String passwordConfirm;
    
    // 비밀번호 일치 검증
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isPasswordMatch() {
        return password != null && password.equals(passwordConfirm);
    }
}
