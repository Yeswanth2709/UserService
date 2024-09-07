package com.example.userservice.services;

import com.example.userservice.models.User;

public interface IAuthService {
    User signup(String email, String password);

    User login(String email, String password);
}
