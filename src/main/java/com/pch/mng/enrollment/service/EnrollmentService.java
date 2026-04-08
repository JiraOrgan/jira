package com.pch.mng.enrollment.service;

import com.pch.mng.course.Course;
import com.pch.mng.course.CourseRepository;
import com.pch.mng.enrollment.Enrollment;
import com.pch.mng.enrollment.EnrollmentRepository;
import com.pch.mng.enrollment.dto.EnrollmentRequest;
import com.pch.mng.enrollment.dto.EnrollmentResponse;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public EnrollmentResponse.Summary enroll(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BusinessException(ErrorCode.ALREADY_ENROLLED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .progress(0)
                .build();

        enrollmentRepository.save(enrollment);
        return new EnrollmentResponse.Summary(enrollment);
    }

    public List<EnrollmentResponse.Summary> getMyEnrollments(Long userId) {
        return enrollmentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(EnrollmentResponse.Summary::new)
                .toList();
    }

    @Transactional
    public EnrollmentResponse.Summary updateProgress(Long enrollmentId, Long userId,
                                                      EnrollmentRequest.UpdateProgress request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        enrollment.updateProgress(request.progress());
        return new EnrollmentResponse.Summary(enrollment);
    }
}
