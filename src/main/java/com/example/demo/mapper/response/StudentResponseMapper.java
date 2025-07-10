package com.example.demo.mapper.response;

import org.mapstruct.Mapper;

import com.example.demo.dto.response.StudentResponse;
import com.example.demo.entity.StudentEntity;
import com.example.demo.mapper.BaseResponseMapper;

@Mapper(componentModel = "spring")
public interface StudentResponseMapper extends BaseResponseMapper<StudentResponse, StudentEntity> {
    // This interface can be extended with additional methods if needed
    
}
