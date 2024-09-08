package com.example.userservice.services;

import com.example.userservice.models.User;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.util.MultiValueMap;

public interface IAuthService {
    User signup(String email, String password);

    Pair<User, MultiValueMap<String,String>> login(String email, String password);
}
