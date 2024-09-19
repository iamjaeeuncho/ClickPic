package com.phoenix.clickpic.member.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.clickpic.member.mapper.UserMapper;
import com.phoenix.clickpic.member.model.User;
import com.phoenix.clickpic.member.service.KakaoService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/api")
public class LoginController { 

    @Autowired
    private KakaoService kakaoService;
    
    @Autowired
    private UserMapper userMapper;  // UserMapper를 주입받음

    // 서버 URL을 상수로 정의
    //private static final String SERVER_URL = "http://localhost:8080";
    private static final String SERVER_URL = "https://4cutstudio.store";
    
    private static final String CLIENT_URL = "https://main.dooslw8q55bm7.amplifyapp.com";
    //private static final String CLIENT_URL = "http://localhost:3000";

    // 카카오 로그인 페이지로 리디렉션
    @GetMapping("/kakao/login")
    public RedirectView redirectToKakao() {
        log.info("카카오 로그인 페이지로 리디렉션 요청됨.");
        String clientId = "fa48f38c035cc445070338897bcbb504"; // 카카오 REST API 키
        String redirectUri = SERVER_URL + "/api/kakao/callback"; // 인가 코드가 반환될 URL
        String kakaoUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
        log.debug("카카오 로그인 URL: " + kakaoUrl);
        return new RedirectView(kakaoUrl);
    }

    // 카카오 로그인 콜백 처리
    @GetMapping("/kakao/callback")
    public RedirectView kakaoCallback(@RequestParam("code") String code, HttpSession session) {
        log.info("카카오 로그인 콜백 요청 처리 중.");
        
        log.debug("받은 인가 코드: " + code);

        String accessToken = kakaoService.getKakaoAccessToken(code);
        if (accessToken == null) {
            log.error("액세스 토큰을 발급받지 못했습니다.");
            return new RedirectView(CLIENT_URL + "/login?error=token");
        }
        log.debug("발급된 액세스 토큰: " + accessToken);

        String userInfoJson = kakaoService.getKakaoUserInfo(accessToken);
        log.debug("받은 사용자 정보: " + userInfoJson);

        if (userInfoJson != null) {
            session.setAttribute("accessToken", accessToken);
            session.setAttribute("userInfo", userInfoJson);
            kakaoService.saveKakaoUserInfo(userInfoJson);  // 사용자 정보를 DB에 저장
            log.info("사용자 정보가 세션에 저장되고 DB에 저장되었습니다.");
        } else {
            log.warn("사용자 정보를 가져오지 못했습니다.");
        }

        return new RedirectView(CLIENT_URL + "/profile?accessToken=" + accessToken);
    }

    @PostMapping("/logout")
    @ResponseBody
    public String logout(HttpSession session, @RequestParam("accessToken") String accessToken) {
        log.info("로그아웃 요청 처리 중.");
        log.debug("받은 액세스 토큰: " + accessToken);

        // 카카오에서 로그아웃
        String logoutResponse = kakaoService.logoutFromKakao(accessToken);
        log.debug("카카오 로그아웃 응답: " + logoutResponse);

        // 서버에서 세션 무효화
        session.invalidate();
        log.info("세션이 무효화되었습니다.");

        return "Logged out successfully. Response from Kakao: " + logoutResponse;
    }

    @GetMapping("/userinfo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserProfile(HttpSession session) {
        log.info("사용자 정보 요청 처리");
        
        String userInfoJson = (String) session.getAttribute("userInfo");
        
        if (userInfoJson == null) {
            log.warn("세션에 사용자 정보가 없습니다. 인증되지 않은 상태로 간주합니다.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userInfo;
        try {
            userInfo = objectMapper.readValue(userInfoJson, Map.class);
            log.debug("세션에서 사용자 정보 파싱 성공.");
        } catch (IOException e) {
            log.error("사용자 정보 파싱 실패", e);
            throw new RuntimeException("사용자 정보 파싱 실패", e);
        }

        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }
    
 // 카카오 로그인 탈퇴
    @PostMapping("/kakao/deactivate")
    @ResponseBody
    public ResponseEntity<String> deactivateKakaoUser(HttpSession session) {
        log.info("카카오 로그인 탈퇴 요청 처리 중.");

        String userInfoJson = (String) session.getAttribute("userInfo");
        if (userInfoJson == null) {
            log.warn("세션에 사용자 정보가 없습니다. 인증되지 않은 상태로 간주합니다.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> userInfoMap = objectMapper.readValue(userInfoJson, Map.class);
            String userid = userInfoMap.get("id").toString();

            kakaoService.deactivateKakaoUser(userid);
            session.invalidate();  // 세션 무효화
            log.info("사용자 {}가 성공적으로 탈퇴되었습니다.", userid);

            return new ResponseEntity<>("User deactivated successfully", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to deactivate Kakao user", e);
            return new ResponseEntity<>("Failed to deactivate Kakao user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 일반 로그인 탈퇴
    @PostMapping("/deactivate")
    @ResponseBody
    public ResponseEntity<String> deactivateUser(@RequestBody Map<String, Object> requestData) {
        log.info("일반 회원 탈퇴 요청 처리 중.");

        // 요청 데이터에서 userId를 받아옴 (로컬 스토리지에서 가져온 값)
        String userId = (String) requestData.get("userId");
        
        if (userId == null || userId.isEmpty()) {
            log.warn("userId가 제공되지 않았습니다. 인증되지 않은 상태로 간주합니다.");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        log.info("탈퇴할 사용자 ID: " + userId);

        try {
            userMapper.deactivateUser(userId); // userId를 기반으로 회원 탈퇴
            log.info("사용자 {}가 성공적으로 탈퇴되었습니다.", userId);

            return new ResponseEntity<>("User deactivated successfully", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to deactivate user", e);
            return new ResponseEntity<>("Failed to deactivate user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<String> registerUser(
        @RequestBody User newUser,  // User 객체로 직접 받기
        HttpSession session) {
        
        newUser.setStatus("Y");  // 회원 상태 설정
        newUser.setCreatedAt(LocalDateTime.now());  // 현재 시간 설정

        try {
            userMapper.insertUser(newUser);  // DB에 사용자 정보 저장
            session.setAttribute("userInfo", newUser);  // 세션에 사용자 정보 저장
            return new ResponseEntity<>("회원가입 성공", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("회원가입 실패", e);
            return new ResponseEntity<>("회원가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User loginUser, HttpSession session) {
        String userId = loginUser.getUserId();
        String password = loginUser.getPassword();

        User user = userMapper.findByUserId(userId); // 유저 아이디로 사용자 찾기

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("userInfo", user); // 세션에 사용자 정보 저장
            
            // 사용자 정보 반환 (userId와 nickname)
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId()); // userId가 null이 아닌지 확인
            response.put("nickname", user.getNickname());

            // 디버깅용 로그 추가
            System.out.println("Returned userId: " + user.getUserId()); // userId 출력
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}


