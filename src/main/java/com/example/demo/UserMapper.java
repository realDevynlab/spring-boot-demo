package com.example.demo;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    User toUser(UserCreationDTO userCreationDTO);

}
