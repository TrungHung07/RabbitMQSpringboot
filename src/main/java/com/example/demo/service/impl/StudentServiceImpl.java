package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.response.StudentResponse;
import com.example.demo.mapper.response.StudentResponseMapper;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.StudentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    // private final StudentRequestMapper requestMapper;
    private final StudentResponseMapper responseMapper;

    @Override
    public List<StudentResponse> getAllStudents() {
        return responseMapper.toListDto(studentRepository.findAll());
    }

    @Override
    public StudentResponse getStudentById(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
