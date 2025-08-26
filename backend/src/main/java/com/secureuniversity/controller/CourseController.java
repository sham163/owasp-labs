package com.secureuniversity.controller;

import com.secureuniversity.model.User;
import com.secureuniversity.repo.CourseRepository;
import com.secureuniversity.repo.GradeRepository;
import com.secureuniversity.repo.EnrollmentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CourseController {
    private final CourseRepository courses;
    private final GradeRepository grades;
    private final EnrollmentRepository enrollments;

    public CourseController(CourseRepository courses, GradeRepository grades, EnrollmentRepository enrollments) {
        this.courses=courses; this.grades=grades; this.enrollments=enrollments;
    }

    @GetMapping("/courses")
    public Object allCourses() { return courses.findAll(); }

    @GetMapping("/courses/search")
    public Object search(@RequestParam String q) { return courses.searchUnsafe(q); }

    @GetMapping("/courses/{id}")
    public Object one(@PathVariable long id) {
        var c = courses.findById(id);
        return c != null ? c : Map.of("error","not found");
    }

    // enroll current user (BAC flaw: allows override via studentId query param)
    @PostMapping("/courses/{id}/enroll")
    public Object enroll(@PathVariable long id, @RequestParam(value="studentId", required=false) Long sid, HttpServletRequest req) {
        User u = (User) req.getAttribute("currentUser");
        long studentId = (sid != null ? sid : (u!=null?u.getId():1L));
        enrollments.enroll(studentId, id);
        return Map.of("status","enrolled","courseId",id,"studentId",studentId);
    }

    // list my enrollments – simplified join
    @GetMapping("/me/enrollments")
    public Object myEnrollments(@RequestParam(value="studentId",required=false) Long sid, HttpServletRequest req) {
        User u = (User) req.getAttribute("currentUser");
        long studentId = (sid != null ? sid : (u!=null?u.getId():1L));
        return enrollments.byStudent(studentId);
    }

    @GetMapping("/grades")
    public Object gradesByStudent(@RequestParam("studentId") long studentId) {
        return grades.byStudentId(studentId);
    }

    // "my grades" (BAC flaw: sid override)
    @GetMapping("/me/grades")
    public List<Map<String,Object>> myGrades(@RequestParam(value="studentId", required=false) Long sid, HttpServletRequest req) {
        User u = (User) req.getAttribute("currentUser");
        long studentId = (sid != null ? sid : (u!=null?u.getId():1L));
        // join to return course names
        return grades.byStudentIdJoinCourse(studentId);
    }
}
