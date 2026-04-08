package com.pch.mng.course.service;

import com.pch.mng.course.Course;
import com.pch.mng.course.CourseRepository;
import com.pch.mng.course.Section;
import com.pch.mng.course.SectionRepository;
import com.pch.mng.course.dto.SectionRequest;
import com.pch.mng.course.dto.SectionResponse;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    public List<SectionResponse.Summary> getSections(Long courseId) {
        return sectionRepository.findByCourseIdOrderByOrderIndex(courseId).stream()
                .map(SectionResponse.Summary::new)
                .toList();
    }

    public SectionResponse.Detail getSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));
        return new SectionResponse.Detail(section);
    }

    @Transactional
    public SectionResponse.Detail createSection(Long courseId, Long userId, SectionRequest.Create request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        Section section = Section.builder()
                .course(course)
                .title(request.title())
                .orderIndex(request.orderIndex())
                .build();

        sectionRepository.save(section);
        return new SectionResponse.Detail(section);
    }

    @Transactional
    public SectionResponse.Detail updateSection(Long sectionId, Long userId, SectionRequest.Update request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        if (!section.getCourse().isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        section.update(request.title(), request.orderIndex());
        return new SectionResponse.Detail(section);
    }

    @Transactional
    public void deleteSection(Long sectionId, Long userId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        if (!section.getCourse().isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        sectionRepository.delete(section);
    }
}
