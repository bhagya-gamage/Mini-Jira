package com.minijira.controller;

import com.minijira.dto.AuthDtos.UserDto;
import com.minijira.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository users;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public List<UserDto> all() { return users.findAll().stream().map(UserDto::from).toList(); }
}
