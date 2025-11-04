package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.UserProfileResponse;
import com.pillmate.pillmate.DTO.UserProfileUpdateRequest;
import com.pillmate.pillmate.DTO.BasicProfileResponse;
import com.pillmate.pillmate.DTO.BasicProfileUpdateRequest;
import com.pillmate.pillmate.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")   // Swagger에서 보일 이름
@RequestMapping("/api/auth/mypage/users")
public class UserController {

	private final UserService userService;

	@Operation(summary = "프로필 조회", description = "userId로 사용자 프로필을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	@GetMapping("/profile/{userId}")
	public UserProfileResponse getProfile(@PathVariable Long userId) {
		return userService.getProfile(userId);
	}

	@Operation(summary = "프로필 수정", description = "userId로 사용자 프로필을 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값이 잘못됨"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	@PutMapping("/profile/{userId}")
	public UserProfileResponse updateProfile(
		@PathVariable Long userId,
		@RequestBody UserProfileUpdateRequest request
	) {
		return userService.updateProfile(userId, request);
	}

	@Operation(summary = "기본 프로필 조회", description = "섭취 페이지에서 사용하는 기본 프로필 정보를 조회합니다.")
	@GetMapping("/basic-profile/{userId}")
	public BasicProfileResponse getBasicProfile(@PathVariable Long userId) {
		return userService.getBasicProfile(userId);
	}


	@Operation(summary = "기본 프로필 수정", description = "섭취 페이지에서 사용하는 기본 프로필 정보를 수정합니다.")
	@PutMapping("/basic-profile/{userId}")
	public BasicProfileResponse updateBasicProfile(
		@PathVariable Long userId,
		@RequestBody BasicProfileUpdateRequest request
	) {
		return userService.updateBasicProfile(userId, request);
	}
}