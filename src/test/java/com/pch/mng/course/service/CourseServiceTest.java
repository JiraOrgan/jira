package com.pch.mng.course.service;

import com.pch.mng.course.Course;
import com.pch.mng.course.CourseRepository;
import com.pch.mng.course.LessonRepository;
import com.pch.mng.course.SectionRepository;
import com.pch.mng.course.dto.CourseRequest;
import com.pch.mng.course.dto.CourseResponse;
import com.pch.mng.enrollment.EnrollmentRepository;
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
class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    private UserAccount instructor;
    private Course course;

    @BeforeEach
    void setUp() {
        instructor = UserAccount.builder()
                .email("instructor@test.com")
                .password("encoded")
                .name("Test Instructor")
                .role(UserRole.INSTRUCTOR)
                .build();
        instructor.setId(1L);

        course = Course.builder()
                .title("Test Course")
                .description("Description")
                .instructor(instructor)
                .build();
        course.setId(10L);
    }

    @Test
    @DisplayName("강의 생성 성공")
    void createCourse_success() {
        given(userAccountRepository.findById(1L)).willReturn(Optional.of(instructor));

        CourseRequest.Create request = new CourseRequest.Create("New Course", "Desc", null);
        CourseResponse.Detail result = courseService.createCourse(1L, request);

        assertThat(result.title()).isEqualTo("New Course");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("존재하지 않는 강사로 강의 생성 시 예외")
    void createCourse_instructorNotFound() {
        given(userAccountRepository.findById(999L)).willReturn(Optional.empty());

        CourseRequest.Create request = new CourseRequest.Create("Course", "Desc", null);

        assertThatThrownBy(() -> courseService.createCourse(999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("강의 수정 - 소유자가 아니면 실패")
    void updateCourse_notOwner_throwsException() {
        given(courseRepository.findById(10L)).willReturn(Optional.of(course));

        CourseRequest.Update request = new CourseRequest.Update("Updated", "Desc", null);

        assertThatThrownBy(() -> courseService.updateCourse(10L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COURSE_ACCESS_DENIED));
    }

    @Test
    @DisplayName("강의 수정 - 소유자 성공")
    void updateCourse_ownerSuccess() {
        given(courseRepository.findById(10L)).willReturn(Optional.of(course));

        CourseRequest.Update request = new CourseRequest.Update("Updated Title", "New Desc", null);
        CourseResponse.Detail result = courseService.updateCourse(10L, 1L, request);

        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.description()).isEqualTo("New Desc");
    }

    @Test
    @DisplayName("강의 삭제 - 소유자만 가능")
    void deleteCourse_ownerSuccess() {
        given(courseRepository.findById(10L)).willReturn(Optional.of(course));

        courseService.deleteCourse(10L, 1L);

        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("강의 삭제 - 소유자가 아니면 실패")
    void deleteCourse_notOwner_throwsException() {
        given(courseRepository.findById(10L)).willReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.deleteCourse(10L, 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COURSE_ACCESS_DENIED));
    }

    @Test
    @DisplayName("존재하지 않는 강의 조회 시 예외")
    void getCourse_notFound() {
        given(courseRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourse(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COURSE_NOT_FOUND));
    }
}
