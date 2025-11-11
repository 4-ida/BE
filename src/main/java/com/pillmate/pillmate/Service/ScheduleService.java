package com.pillmate.pillmate.Service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pillmate.pillmate.Domain.Schedule;
import com.pillmate.pillmate.Domain.ScheduleStatus;
import com.pillmate.pillmate.Domain.MedicationIntake;
import com.pillmate.pillmate.DTO.ScheduleRequest;
import com.pillmate.pillmate.DTO.ScheduleResponse;
import com.pillmate.pillmate.DTO.ScheduleUpdateRequest;
import com.pillmate.pillmate.DTO.ScheduleUpdateResponse;
import com.pillmate.pillmate.DTO.DrugDetailResponse;
import com.pillmate.pillmate.DTO.BanTimerResponse;
import com.pillmate.pillmate.Repository.ScheduleRepository;
import com.pillmate.pillmate.Repository.MedicationIntakeRepository;
import com.pillmate.pillmate.Service.MedicationIntakeService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final DrugDetailService drugDetailService;
    private final MedicationIntakeRepository medicationIntakeRepository;
    private final MedicationIntakeService medicationIntakeService;
    
    @Transactional
    public ScheduleResponse createSchedule(Long userId, ScheduleRequest request) {
        // 복용 날짜와 알림 시각 검증
        if (request.getAlarmAt() != null && !request.getDate().equals(request.getAlarmAt().toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "복용 날짜와 알림 시각의 날짜가 일치해야 합니다");
        }
        
        DrugDetailResponse drugDetail = fetchDrugDetailOrThrow(request.getDrugId());
        String resolvedDrugName = resolveDrugName(request, drugDetail);
        boolean alarmEnabled = resolveAlarmEnabled(request);
        
        // 등록 시에는 항상 SCHEDULED로 저장 (기본값)
        ScheduleStatus resolvedStatus = ScheduleStatus.SCHEDULED;

        // 복용 기간 검증 (startDate, endDate가 있으면)
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "복용 시작일은 종료일보다 이전이어야 합니다");
            }
            // date가 기간 내에 있는지 검증 (선택사항)
            if (request.getDate().isBefore(request.getStartDate()) || request.getDate().isAfter(request.getEndDate())) {
                // 경고만 하고 계속 진행 (date가 기간 밖에 있어도 허용)
            }
        }
        
        // 일정 생성 (단일 날짜, 복용 기간은 표시용)
        Schedule schedule = Schedule.builder()
                .userId(userId)
                .drugId(request.getDrugId())
                .drugName(resolvedDrugName)
                .dose(request.getDose())
                .date(request.getDate())
                .alarmAt(request.getAlarmAt())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .memo(request.getMemo())
                .alarmEnabled(alarmEnabled)
                .repeatRule(null)
                .status(resolvedStatus)
                .build();
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        // 등록 시에는 plan과 status가 없으므로 null 전달 (응답에서 plan은 "SCHEDULED", status는 null 반환)
        return ScheduleResponse.from(savedSchedule, null, null);
    }
    
    /**
     * ID로 단일 일정 조회
     * @param scheduleId 일정 ID
     * @return 일정 정보
     */
    @Transactional
    public ScheduleResponse getScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다"));
        
        LocalDate today = LocalDate.now();
        
        // 날짜가 지난 SCHEDULED 일정을 자동으로 MISSED로 변경
        if (schedule.getDate().isBefore(today) && schedule.getStatus() == ScheduleStatus.SCHEDULED) {
            schedule.updateStatus(ScheduleStatus.MISSED);
            scheduleRepository.save(schedule);
        }
        
        return ScheduleResponse.from(schedule);
    }
    
    /**
     * 특정 날짜의 일정 조회
     * @param userId 사용자 ID
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 해당 날짜의 일정 목록
     */
    @Transactional(readOnly = false)
    public List<ScheduleResponse> getSchedulesByDate(Long userId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findByUserIdAndDate(userId, date);
        LocalDate today = LocalDate.now();
        
        // 날짜가 지난 SCHEDULED 일정을 자동으로 MISSED로 변경
        schedules.forEach(schedule -> {
            if (schedule.getDate().isBefore(today) && schedule.getStatus() == ScheduleStatus.SCHEDULED) {
                schedule.updateStatus(ScheduleStatus.MISSED);
                scheduleRepository.save(schedule);
            }
        });
        
        return schedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 기간의 일정 조회
     * @param userId 사용자 ID
     * @param from 시작 날짜 (YYYY-MM-DD 형식)
     * @param to 종료 날짜 (YYYY-MM-DD 형식)
     * @return 해당 기간의 일정 목록
     */
    @Transactional(readOnly = false)
    public List<ScheduleResponse> getSchedulesByDateRange(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 이전이어야 합니다");
        }
        
        List<Schedule> schedules = scheduleRepository.findByDateRange(from, to);
        // 사용자 필터링
        List<Schedule> userSchedules = schedules.stream()
                .filter(s -> s.getUserId().equals(userId))
                .collect(Collectors.toList());
        
        LocalDate today = LocalDate.now();
        
        // 날짜가 지난 SCHEDULED 일정을 자동으로 MISSED로 변경
        userSchedules.forEach(schedule -> {
            if (schedule.getDate().isBefore(today) && schedule.getStatus() == ScheduleStatus.SCHEDULED) {
                schedule.updateStatus(ScheduleStatus.MISSED);
                scheduleRepository.save(schedule);
            }
        });
        
        return userSchedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 월의 일정 조회 (날짜별로 그룹화)
     * @param userId 사용자 ID
     * @param year 년도 (예: 2025)
     * @param month 월 (1-12)
     * @return 해당 월의 일정 목록 (날짜별로 그룹화)
     */
    public java.util.Map<LocalDate, List<ScheduleResponse>> getSchedulesByMonth(Long userId, int year, int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "월은 1부터 12 사이의 값이어야 합니다");
        }
        
        // 해당 월의 첫 날과 마지막 날 계산
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        
        // 해당 월의 모든 일정 조회
        List<ScheduleResponse> schedules = getSchedulesByDateRange(userId, firstDayOfMonth, lastDayOfMonth);
        
        // 날짜별로 그룹화
        return schedules.stream()
                .collect(Collectors.groupingBy(ScheduleResponse::getDate));
    }
    
    /**
     * 일정 수정
     * @param scheduleId 일정 ID
     * @param userId 사용자 ID
     * @param request 수정 요청 데이터
     * @return 수정된 일정 정보
     */
    @Transactional
    public ScheduleUpdateResponse updateSchedule(Long scheduleId, Long userId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다"));
        
        // 사용자 검증
        if (!schedule.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 일정을 수정할 권한이 없습니다");
        }
        
        // 약품 변경 처리 (drugId가 제공되면 새로운 약품으로 변경)
        Long targetDrugId = request.getDrugId() != null ? request.getDrugId() : schedule.getDrugId();
        
        // 약품명 동기화 (등록 API처럼 drugId로 식약처 API에서 공식 약품명 가져오기)
        DrugDetailResponse drugDetail = fetchDrugDetailOrThrow(targetDrugId);
        String officialName = request.getName() != null && !request.getName().trim().isEmpty()
                ? resolveDrugName(request.getName(), drugDetail)
                : drugDetail.getName();
        
        // drugId 변경 처리
        if (request.getDrugId() != null && !request.getDrugId().equals(schedule.getDrugId())) {
            schedule.setDrugId(request.getDrugId());
        }
        
        // 약품명 업데이트 (공식 약품명으로 동기화)
        if (officialName != null && !officialName.equals(schedule.getDrugName())) {
            schedule.setDrugName(officialName);
        }
        
        // 복용 날짜 수정
        if (request.getDate() != null && !request.getDate().equals(schedule.getDate())) {
            schedule.updateDate(request.getDate());
        }
        
        // 복용 예정 시각 수정
        if (request.getAlarmAt() != null && !request.getAlarmAt().equals(schedule.getAlarmAt())) {
            // 날짜와 알림 시각의 날짜가 일치하는지 검증
            LocalDate scheduleDate = request.getDate() != null ? request.getDate() : schedule.getDate();
            if (!scheduleDate.equals(request.getAlarmAt().toLocalDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "복용 날짜와 알림 시각의 날짜가 일치해야 합니다");
            }
            schedule.updateAlarmAt(request.getAlarmAt());
        }
        
        // 복용 기간 수정 (표시용)
        if (request.getStartDate() != null && !request.getStartDate().equals(schedule.getStartDate())) {
            schedule.updateStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null && !request.getEndDate().equals(schedule.getEndDate())) {
            schedule.updateEndDate(request.getEndDate());
        }
        
        // 복용 기간 검증 (startDate, endDate가 모두 있으면)
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "복용 시작일은 종료일보다 이전이어야 합니다");
            }
        }
        
        // 복용량 수정
        if (request.getDose() != null && !request.getDose().equals(schedule.getDose())) {
            schedule.updateDose(request.getDose());
        }
        
        // 메모 수정
        if (request.getMemo() != null && !java.util.Objects.equals(request.getMemo(), schedule.getMemo())) {
            schedule.updateMemo(request.getMemo());
        }
        
        // 상태 수정 및 금지 타이머 계산
        BanTimerResponse caffeineBanTimer = null;
        BanTimerResponse alcoholBanTimer = null;
        ScheduleStatus previousStatus = schedule.getStatus();
        
        // plan 처리 우선 (plan이 CANCELLED이면 상태를 CANCELLED로 변경)
        boolean planCancelled = false;
        if (request.getPlan() != null) {
            String planValue = request.getPlan().trim().toUpperCase();
            if ("CANCELLED".equals(planValue)) {
                schedule.updateStatus(ScheduleStatus.CANCELLED);
                planCancelled = true;
            } else if ("SCHEDULED".equals(planValue)) {
                // CANCELLED에서 SCHEDULED로 복구 (단, status가 TAKEN/MISSED가 아니면)
                if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
                    schedule.updateStatus(ScheduleStatus.SCHEDULED);
                }
            }
        }
        
        // 사용자가 명시적으로 status를 변경하려고 하는 경우
        if (request.getStatus() != null && !planCancelled) {
            String statusValue = request.getStatus().trim().toUpperCase();
            ScheduleStatus newStatus = resolveStatus(statusValue);
            
            // 상태 변경 (plan이 CANCELLED가 아닌 경우에만)
            schedule.updateStatus(newStatus);
            
            // TAKEN 상태로 변경된 경우 금지 타이머 계산
            if (newStatus == ScheduleStatus.TAKEN && previousStatus != ScheduleStatus.TAKEN) {
                // 복용 기록 생성 (복용 완료 시각은 현재 시각 사용)
                LocalDateTime takenAt = LocalDateTime.now();
                
                // 중복 방지: 이미 복용 기록이 있는지 확인
                List<MedicationIntake> existingIntakes = medicationIntakeRepository.findByScheduleId(scheduleId);
                if (existingIntakes.isEmpty()) {
                    MedicationIntake medicationIntake = MedicationIntake.builder()
                            .scheduleId(scheduleId)
                            .drugId(schedule.getDrugId())
                            .takenAt(takenAt)
                            .build();
                    medicationIntakeRepository.save(medicationIntake);
                } else {
                    // 이미 기록이 있으면 가장 최근 기록 사용
                    takenAt = existingIntakes.stream()
                            .map(MedicationIntake::getTakenAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(LocalDateTime.now());
                }
                
                // 금지 타이머 계산 (카페인, 알코올)
                List<BanTimerResponse> banTimers = medicationIntakeService.calculateBanTimersInternal(takenAt, schedule.getDrugId());
                
                // 카페인과 알코올 타이머 분리
                for (BanTimerResponse timer : banTimers) {
                    if ("caffeine".equals(timer.getType())) {
                        caffeineBanTimer = timer;
                    } else if ("alcohol".equals(timer.getType())) {
                        alcoholBanTimer = timer;
                    }
                }
            }
        } else if (request.getStatus() == null && !planCancelled) {
            // 사용자가 status를 명시적으로 변경하지 않은 경우
            // 날짜가 지났고 SCHEDULED 상태이면 자동으로 MISSED로 변경
            // 단, plan이 CANCELLED로 설정된 경우는 제외
            LocalDate finalScheduleDate = request.getDate() != null ? request.getDate() : schedule.getDate();
            LocalDate today = LocalDate.now();
            
            // 날짜가 오늘보다 과거이고, 현재 상태가 SCHEDULED이면 MISSED로 자동 변경
            // CANCELLED 상태는 자동 변경하지 않음
            if (finalScheduleDate.isBefore(today) && schedule.getStatus() == ScheduleStatus.SCHEDULED) {
                schedule.updateStatus(ScheduleStatus.MISSED);
            }
        }
        
        // 알림 설정 수정
        if (request.getAlarm() != null && request.getAlarm().getEnabled() != null) {
            boolean newAlarmEnabled = request.getAlarm().getEnabled();
            if (newAlarmEnabled != schedule.getAlarmEnabled()) {
                schedule.updateAlarmEnabled(newAlarmEnabled);
            }
        }
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        // 응답 생성
        return buildUpdateResponse(savedSchedule, caffeineBanTimer, alcoholBanTimer);
    }
    
    /**
     * 일정 삭제
     * @param scheduleId 일정 ID
     * @param userId 사용자 ID
     * @return 삭제된 일정 ID
     */
    @Transactional
    public Long deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다"));
        
        // 사용자 검증
        if (!schedule.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 일정을 삭제할 권한이 없습니다");
        }
        
        scheduleRepository.delete(schedule);
        return scheduleId;
    }

    private boolean resolveAlarmEnabled(ScheduleRequest request) {
        if (request.getAlarm() == null || request.getAlarm().getEnabled() == null) {
            return false;
        }
        return request.getAlarm().getEnabled();
    }

    private DrugDetailResponse fetchDrugDetailOrThrow(Long drugId) {
        try {
            return drugDetailService.fetchDrugDetail(String.valueOf(drugId));
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "식약처에서 해당 약품을 찾을 수 없습니다");
            }
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 약품 ID입니다");
        }
    }

    private String resolveDrugName(ScheduleRequest request, DrugDetailResponse drugDetail) {
        String officialName = drugDetail.getName();
        if (officialName != null && !officialName.isBlank()) {
            return officialName;
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            return request.getName();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "약품명이 비어 있습니다");
    }
    
    private String resolveDrugName(String requestedName, DrugDetailResponse drugDetail) {
        String officialName = drugDetail.getName();
        if (officialName != null && !officialName.isBlank()) {
            return officialName;
        }
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "약품명이 비어 있습니다");
    }
    
    private ScheduleStatus resolveStatus(String statusValue) {
        try {
            return ScheduleStatus.valueOf(statusValue);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 값입니다: " + statusValue);
        }
    }
    
    /**
     * Schedule 엔티티를 ScheduleUpdateResponse로 변환
     */
    private ScheduleUpdateResponse buildUpdateResponse(Schedule schedule, BanTimerResponse caffeineBanTimer, BanTimerResponse alcoholBanTimer) {
        ScheduleStatus currentStatus = schedule.getStatus();
        String resolvedPlan = currentStatus == ScheduleStatus.CANCELLED ? ScheduleStatus.CANCELLED.name() : ScheduleStatus.SCHEDULED.name();
        String resolvedStatus = switch (currentStatus) {
            case TAKEN -> ScheduleStatus.TAKEN.name();
            case MISSED -> ScheduleStatus.MISSED.name();
            default -> null;
        };
        
        return ScheduleUpdateResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .drugId(schedule.getDrugId())
                .name(schedule.getDrugName())
                .dose(schedule.getDose())
                .date(schedule.getDate())
                .alarmAt(schedule.getAlarmAt())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .memo(schedule.getMemo())
                .plan(resolvedPlan)
                .status(resolvedStatus)
                .alarm(ScheduleUpdateResponse.AlarmSettings.builder()
                        .enabled(schedule.getAlarmEnabled())
                        .build())
                .caffeineBanTimer(caffeineBanTimer)
                .alcoholBanTimer(alcoholBanTimer)
                .build();
    }
}
