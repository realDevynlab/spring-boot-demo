package com.example.demo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationDTO userCreationDTO);

    @Mapping(target = "roles", source = "roles")
        // Use the helper method for mapping roles
    UserDTO toUserDTO(User user);

    default Set<String> mapRolesToRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
