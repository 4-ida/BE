package com.pillmate.pillmate.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "약관 동의 정보")
public class ConsentDto {
    
    @Schema(description = "서비스 이용약관 동의 여부 (필수)", example = "true", required = true)
    private Boolean termsOfService;
    
    @Schema(description = "개인정보 처리방침 동의 여부 (필수)", example = "true", required = true)
    private Boolean privacyPolicy;
    
    @Schema(description = "데이터 활용 동의 여부 (선택)", example = "true")
    private Boolean dataUsage;
}

