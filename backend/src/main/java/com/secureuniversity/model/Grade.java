package com.secureuniversity.model;

public class Grade {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String gradeValue;

    public Grade() {}
    public Grade(Long id, Long studentId, Long courseId, String gradeValue) {
        this.id=id; this.studentId=studentId; this.courseId=courseId; this.gradeValue=gradeValue;
    }
    public Long getId(){ return id; }
    public Long getStudentId(){ return studentId; }
    public Long getCourseId(){ return courseId; }
    public String getGradeValue(){ return gradeValue; }
    public void setId(Long id){ this.id=id; }
    public void setStudentId(Long studentId){ this.studentId=studentId; }
    public void setCourseId(Long courseId){ this.courseId=courseId; }
    public void setGradeValue(String gradeValue){ this.gradeValue=gradeValue; }
}
