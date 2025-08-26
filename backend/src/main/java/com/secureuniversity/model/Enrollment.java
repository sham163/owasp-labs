package com.secureuniversity.model;

import java.time.LocalDateTime;

public class Enrollment {
    private Long id;
    private Long studentId;
    private Long courseId;
    private LocalDateTime createdAt;

    public Enrollment() {}
    public Enrollment(Long id, Long studentId, Long courseId, LocalDateTime createdAt){
        this.id=id; this.studentId=studentId; this.courseId=courseId; this.createdAt=createdAt;
    }

    public Long getId(){ return id; }
    public Long getStudentId(){ return studentId; }
    public Long getCourseId(){ return courseId; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setId(Long id){ this.id=id; }
    public void setStudentId(Long studentId){ this.studentId=studentId; }
    public void setCourseId(Long courseId){ this.courseId=courseId; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt=createdAt; }
}
