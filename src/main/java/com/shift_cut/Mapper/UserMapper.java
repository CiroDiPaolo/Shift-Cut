package com.shift_cut.Mapper;

import com.shift_cut.Model.DTO.UserDTO;
import com.shift_cut.Model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDTO toDto(UserEntity user);
    UserEntity toEntity(UserDTO dto);
}
