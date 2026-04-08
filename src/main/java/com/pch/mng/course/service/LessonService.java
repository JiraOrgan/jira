package com.pch.mng.course.service;

import com.pch.mng.course.Lesson;
import com.pch.mng.course.LessonRepository;
import com.pch.mng.course.Section;
import com.pch.mng.course.SectionRepository;
import com.pch.mng.course.dto.LessonRequest;
import com.pch.mng.course.dto.LessonResponse;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;

    public List<LessonResponse.Summary> getLessons(Long sectionId) {
        return lessonRepository.findBySectionIdOrderByOrderIndex(sectionId).stream()
                .map(LessonResponse.Summary::new)
                .toList();
    }

    public LessonResponse.Detail getLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        return new LessonResponse.Detail(lesson);
    }

    @Transactional
    public LessonResponse.Detail createLesson(Long sectionId, Long userId, LessonRequest.Create request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        if (!section.getCourse().isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        Lesson lesson = Lesson.builder()
                .section(section)
                .title(request.title())
                .content(request.content())
                .contentType(request.contentType())
                .videoUrl(request.videoUrl())
                .orderIndex(request.orderIndex())
                .durationMinutes(request.durationMinutes())
                .build();

        lessonRepository.save(lesson);
        return new LessonResponse.Detail(lesson);
    }

    @Transactional
    public LessonResponse.Detail updateLesson(Long lessonId, Long userId, LessonRequest.Update request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (!lesson.getSection().getCourse().isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        lesson.update(
                request.title(), request.content(), request.contentType(),
                request.videoUrl(), request.orderIndex(), request.durationMinutes()
        );
        return new LessonResponse.Detail(lesson);
    }

    @Transactional
    public void deleteLesson(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (!lesson.getSection().getCourse().isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        lessonRepository.delete(lesson);
    }
}
