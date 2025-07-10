package com.example.demo.mapper.request;

import org.mapstruct.Mapper;

import com.example.demo.dto.request.StudentRequest;
import com.example.demo.entity.StudentEntity;
import com.example.demo.mapper.BaseRequestMapper;

@Mapper(componentModel = "spring")
public interface StudentRequestMapper extends BaseRequestMapper<StudentRequest, StudentEntity> {
    
}
