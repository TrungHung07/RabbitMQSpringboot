package com.example.demo.mapper.request;

import org.mapstruct.Mapper;

import com.example.demo.dto.request.ClassRequest;
import com.example.demo.entity.ClassEntity;
import com.example.demo.mapper.BaseRequestMapper;

@Mapper(componentModel = "spring")
public interface ClassRequestMapper extends BaseRequestMapper<ClassRequest, ClassEntity> {

}
