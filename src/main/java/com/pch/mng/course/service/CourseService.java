package com.pch.mng.course.service;

import com.pch.mng.course.Course;
import com.pch.mng.course.CourseRepository;
import com.pch.mng.course.Lesson;
import com.pch.mng.course.LessonRepository;
import com.pch.mng.course.Section;
import com.pch.mng.course.SectionRepository;
import com.pch.mng.course.dto.CourseDetailResponse;
import com.pch.mng.course.dto.CourseRequest;
import com.pch.mng.course.dto.CourseResponse;
import com.pch.mng.enrollment.EnrollmentRepository;
import com.pch.mng.global.common.PageResponse;
import com.pch.mng.global.enums.CourseStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserAccountRepository userAccountRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    public PageResponse<CourseResponse.Summary> getPublishedCourses(String keyword, Pageable pageable) {
        Page<CourseResponse.Summary> page;
        if (keyword != null && !keyword.isBlank()) {
            page = courseRepository.findByStatusAndKeyword(CourseStatus.PUBLISHED, keyword, pageable)
                    .map(CourseResponse.Summary::new);
        } else {
            page = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable)
                    .map(CourseResponse.Summary::new);
        }
        return PageResponse.from(page);
    }

    public PageResponse<CourseResponse.Summary> getAllCourses(Pageable pageable) {
        Page<CourseResponse.Summary> page = courseRepository.findAll(pageable)
                .map(CourseResponse.Summary::new);
        return PageResponse.from(page);
    }

    public CourseDetailResponse getPublicCourseDetail(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        long enrollmentCount = enrollmentRepository.countByCourseId(id);

        List<Section> sections = sectionRepository.findByCourseIdOrderByOrderIndex(id);
        List<CourseDetailResponse.SectionSummary> sectionSummaries = sections.stream()
                .map(section -> {
                    List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderIndex(section.getId());
                    List<CourseDetailResponse.LessonSummary> lessonSummaries = lessons.stream()
                            .map(lesson -> new CourseDetailResponse.LessonSummary(
                                    lesson.getId(),
                                    lesson.getTitle(),
                                    lesson.getOrderIndex(),
                                    lesson.getDurationMinutes()))
                            .toList();
                    return new CourseDetailResponse.SectionSummary(
                            section.getId(),
                            section.getTitle(),
                            section.getOrderIndex(),
                            lessonSummaries);
                })
                .toList();

        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getInstructor() != null ? course.getInstructor().getName() : null,
                course.getStatus(),
                enrollmentCount,
                sectionSummaries,
                course.getCreatedAt()
        );
    }

    public CourseResponse.Detail getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        return new CourseResponse.Detail(course);
    }

    @Transactional
    public CourseResponse.Detail createCourse(Long instructorId, CourseRequest.Create request) {
        UserAccount instructor = userAccountRepository.findById(instructorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .instructor(instructor)
                .thumbnailUrl(request.thumbnailUrl())
                .build();

        courseRepository.save(course);
        return new CourseResponse.Detail(course);
    }

    @Transactional
    public CourseResponse.Detail updateCourse(Long courseId, Long userId, CourseRequest.Update request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        course.update(request.title(), request.description(), request.thumbnailUrl());
        return new CourseResponse.Detail(course);
    }

    @Transactional
    public void deleteCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        courseRepository.delete(course);
    }
}
