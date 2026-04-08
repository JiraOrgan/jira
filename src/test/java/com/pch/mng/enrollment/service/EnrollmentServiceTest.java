package com.pch.mng.enrollment.service;

import com.pch.mng.course.Course;
import com.pch.mng.course.CourseRepository;
import com.pch.mng.enrollment.Enrollment;
import com.pch.mng.enrollment.EnrollmentRepository;
import com.pch.mng.enrollment.dto.EnrollmentRequest;
import com.pch.mng.enrollment.dto.EnrollmentResponse;
import com.pch.mng.global.enums.EnrollmentStatus;
import com.pch.mng.global.enums.UserRole;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @InjectMocks
    private EnrollmentService enrollmentService;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    private UserAccount learner;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        learner = UserAccount.builder()
                .email("learner@test.com")
                .password("encoded")
                .name("Learner")
                .role(UserRole.LEARNER)
                .build();
        learner.setId(1L);

        course = Course.builder()
                .title("Course")
                .description("Desc")
                .build();
        course.setId(10L);

        enrollment = Enrollment.builder()
                .user(learner)
                .course(course)
                .progress(50)
                .build();
        enrollment.setId(100L);
    }

    @Test
    @DisplayName("수강 신청 성공")
    void enroll_success() {
        given(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).willReturn(false);
        given(userAccountRepository.findById(1L)).willReturn(Optional.of(learner));
        given(courseRepository.findById(10L)).willReturn(Optional.of(course));
        given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment);

        EnrollmentResponse.Summary result = enrollmentService.enroll(1L, 10L);

        assertThat(result).isNotNull();
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("중복 수강 시 실패")
    void enroll_duplicate_throwsException() {
        given(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).willReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_ENROLLED));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 수강 신청 시 실패")
    void enroll_userNotFound() {
        given(enrollmentRepository.existsByUserIdAndCourseId(999L, 10L)).willReturn(false);
        given(userAccountRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(999L, 10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("진도율 100% 시 COMPLETED 상태 변경")
    void updateProgress_100_completesEnrollment() {
        given(enrollmentRepository.findById(100L)).willReturn(Optional.of(enrollment));

        EnrollmentRequest.UpdateProgress request = new EnrollmentRequest.UpdateProgress(100);
        enrollmentService.updateProgress(100L, 1L, request);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollment.getProgress()).isEqualTo(100);
    }

    @Test
    @DisplayName("진도율 업데이트 성공 (100 미만)")
    void updateProgress_belowHundred_staysActive() {
        given(enrollmentRepository.findById(100L)).willReturn(Optional.of(enrollment));

        EnrollmentRequest.UpdateProgress request = new EnrollmentRequest.UpdateProgress(80);
        enrollmentService.updateProgress(100L, 1L, request);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getProgress()).isEqualTo(80);
    }

    @Test
    @DisplayName("다른 사용자의 수강 진도 업데이트 시 실패")
    void updateProgress_notOwner_throwsException() {
        given(enrollmentRepository.findById(100L)).willReturn(Optional.of(enrollment));

        EnrollmentRequest.UpdateProgress request = new EnrollmentRequest.UpdateProgress(80);

        assertThatThrownBy(() -> enrollmentService.updateProgress(100L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("존재하지 않는 수강 정보 진도 업데이트 시 실패")
    void updateProgress_enrollmentNotFound() {
        given(enrollmentRepository.findById(999L)).willReturn(Optional.empty());

        EnrollmentRequest.UpdateProgress request = new EnrollmentRequest.UpdateProgress(50);

        assertThatThrownBy(() -> enrollmentService.updateProgress(999L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND));
    }
}
