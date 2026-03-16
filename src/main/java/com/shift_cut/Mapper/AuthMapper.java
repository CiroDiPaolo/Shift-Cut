package com.shift_cut.Mapper;

import com.shift_cut.Config.Auth.RegisterRequest;
import com.shift_cut.Model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "password", ignore = true) // password handled separately (encoded)
    UserEntity toEntity(RegisterRequest request);
}

