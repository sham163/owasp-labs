package com.secureuniversity.controller;

import com.secureuniversity.repo.CourseRepository;
import com.secureuniversity.repo.GradeRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CourseController {
    private final CourseRepository courses;
    private final GradeRepository grades;

    public CourseController(CourseRepository courses, GradeRepository grades) {
        this.courses=courses; this.grades=grades;
    }

    @GetMapping("/courses")
    public Object allCourses() { return courses.findAll(); }

    @GetMapping("/courses/search")
    public Object search(@RequestParam String q) {
        return courses.searchUnsafe(q);
    }

    @GetMapping("/grades")
    public Object gradesByStudent(@RequestParam("studentId") long studentId) {
        return grades.byStudentId(studentId);
    }
}
