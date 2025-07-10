package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.response.StudentResponse;

public interface StudentService {
    List<StudentResponse> getAllStudents();
    StudentResponse getStudentById(int id);
}
