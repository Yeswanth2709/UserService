package com.example.userservice.controllers;

import com.example.userservice.dtos.*;
import com.example.userservice.exceptions.AuthenticationFailedException;
import com.example.userservice.models.User;
import com.example.userservice.services.IAuthService;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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
            Pair<User, MultiValueMap<String, String>> loginUser = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            if (loginUser == null) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            return new ResponseEntity<>(from(loginUser.a),loginUser.b, HttpStatus.OK);
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
    public ResponseEntity<String> validateToken(@RequestBody ValidateTokenRequestDto validateTokenRequestDto) throws AuthenticationFailedException {
        try {
            Boolean result = authService.validateToken(validateTokenRequestDto.getToken(), validateTokenRequestDto.getUserId());
            if (!result) {
                throw new AuthenticationFailedException("Bad credentials");
            }
            return new ResponseEntity<>("valid token",HttpStatus.OK);
        }catch (AuthenticationFailedException ex){
            throw ex;
        }

    }

    private UserDto from(User user){
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
