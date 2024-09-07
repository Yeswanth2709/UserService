package com.example.userservice.controllers;

import com.example.userservice.dtos.*;
import com.example.userservice.models.User;
import com.example.userservice.services.IAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequestDto signupRequestDto){
        User user = authService.signup(signupRequestDto.getEmail(), signupRequestDto.getPassword());
        return new ResponseEntity<>(from(user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto){
        try {
            User user = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            if (user == null) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            return new ResponseEntity<>(from(user), HttpStatus.OK);
        }
        catch(IllegalArgumentException ex){
            throw ex;
        }
    }

    @PostMapping("/logout")
    public void logout(@RequestBody LogoutRequestDto logoutRequestDto){

    }

    @PostMapping("/forgotPassword")
    public void forgotPassword(@RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto){

    }

    @PostMapping("/validateToken")
    public void validateToken(@RequestBody ValidateTokenRequestDto validateTokenRequestDto){

    }

    private UserDto from(User user){
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
