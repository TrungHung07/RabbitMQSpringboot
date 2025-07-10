package com.example.demo.mapper.response;

import org.mapstruct.Mapper;

import com.example.demo.dto.response.ClassResponse;
import com.example.demo.entity.ClassEntity;
import com.example.demo.mapper.BaseResponseMapper;

@Mapper(componentModel = "spring")
public interface ClassResponseMapper extends BaseResponseMapper<ClassResponse, ClassEntity> {
    // This interface can be extended with additional methods if needed
    
}
