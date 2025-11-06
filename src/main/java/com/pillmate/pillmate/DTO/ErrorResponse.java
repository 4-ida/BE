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
@Schema(description = "에러 응답")
public class ErrorResponse {
    
    @Schema(description = "에러 메시지", example = "존재하지 않는 게시물입니다")
    private String message;
    
    @Schema(description = "에러 타입", example = "NOT_FOUND")
    private String error;
    
    @Schema(description = "HTTP 상태 코드", example = "404")
    private int status;
}

