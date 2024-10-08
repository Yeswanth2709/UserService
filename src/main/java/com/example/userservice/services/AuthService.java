package com.example.userservice.services;

import com.example.userservice.models.Session;
import com.example.userservice.models.SessionStatus;
import com.example.userservice.models.User;
import com.example.userservice.repositories.SessionRepository;
import com.example.userservice.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService implements IAuthService {
    private UserRepository userRepository;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private SessionRepository sessionRepository;
    private SecretKey secretKey;
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,SessionRepository sessionRepository,SecretKey secretKey) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sessionRepository = sessionRepository;
        this.secretKey = secretKey;
    }

    @Override
    public User signup(String email, String password){
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            return optionalUser.get();
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        return userRepository.save(user);
    }
    @Override
    public Pair<User, MultiValueMap<String,String>> login(String email, String password){
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isEmpty()){
            return null;
        }
        User user=optionalUser.get();

        if(!bCryptPasswordEncoder.matches(password,user.getPassword())){
            return null;
        }
        //generating token
        Map<String,Object> jwtData = new HashMap<>();
        jwtData.put("email",user.getEmail());
        jwtData.put("roles",user.getRoles());
        Long nowInMillis = System.currentTimeMillis();
        jwtData.put("iat",nowInMillis);
        jwtData.put("exp",nowInMillis+24*60*60*1000);

//        MacAlgorithm algorithm= Jwts.SIG.HS256;
//        SecretKey secretKey=algorithm.key().build();
        String token=Jwts.builder().claims(jwtData).signWith(secretKey).compact();

        Session session=new Session();
        session.setUser(user);
        session.setToken(token);
        session.setSessionStatus(SessionStatus.ACTIVE);
        sessionRepository.save(session);
        MultiValueMap<String,String> headers=new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.SET_COOKIE,token);
        return new Pair<>(user,headers);
    }
    public Boolean validateToken(String token,Long userId){
        Optional<Session> optionalSession = sessionRepository.findByTokenEquals(token);
        if(optionalSession.isEmpty()){
            return false;
        }
        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        Long expiryInEpoch = (Long)claims.get("exp");
        Long currentTime = System.currentTimeMillis();
        if(currentTime>expiryInEpoch){
            return false;
        }
        Optional<User> optionalUser = userRepository.findById(userId);
        String userEmail = optionalUser.get().getEmail();
        if(!userEmail.equals(claims.get("email"))){
            return false;
        }
        return true;
    }
}
