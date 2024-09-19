package com.phoenix.clickpic.member.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.clickpic.member.mapper.UserMapper;
import com.phoenix.clickpic.member.model.User;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class KakaoService {

	// 서버 URL을 상수로 정의
    //private static final String SERVER_URL = "http://localhost:8080";
	private static final String SERVER_URL = "https://4cutstudio.store";

    @Autowired
    private UserMapper userMapper;

    public String saveKakaoUserInfo(String userInfoJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> userInfoMap = objectMapper.readValue(userInfoJson, Map.class);

            User user = new User();
            user.setUserId(userInfoMap.get("id").toString());

            Map<String, Object> propertiesMap = (Map<String, Object>) userInfoMap.get("properties");
            user.setNickname((String) propertiesMap.get("nickname"));

            user.setStatus("Y");  // 기본 회원 상태 설정

            int userExists = userMapper.checkUserExists(user.getUserId());

            if (userExists > 0) {
                userMapper.updateUser(user); // 기존 사용자 정보 업데이트
                log.info("User with ID {} updated successfully.", user.getUserId());
            } else {
                user.setCreatedAt(LocalDateTime.now()); // 현재 날짜와 시간 설정
                userMapper.insertUser(user); // 새로운 사용자 추가
                log.info("New user with ID {} added successfully.", user.getUserId());
            }

            return "User information saved or updated successfully";
        } catch (Exception e) {
            log.error("Failed to save or update user information", e);
            throw new RuntimeException("Failed to save or update user information", e);
        }
    }


   
    public String getKakaoAccessToken(String code) {
        String clientId = "fa48f38c035cc445070338897bcbb504"; // 카카오 REST API 키
        String redirectUri = SERVER_URL + "/api/kakao/callback"; // 인가 코드가 반환된 URL

        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // MultiValueMap을 사용하여 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        String accessToken = (String) response.getBody().get("access_token");
        return accessToken;
    }

    public String getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        return response.getBody(); // JSON 형식의 사용자 정보 반환
    }

    public String logoutFromKakao(String accessToken) {
        String logoutUrl = "https://kapi.kakao.com/v1/user/logout";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(logoutUrl, HttpMethod.POST, request, String.class);

        return response.getBody(); // 로그아웃 결과 반환
    }
    
    public String deactivateKakaoUser(String userid) {
        try {
            userMapper.deactivateUser(userid);
            log.info("User with ID {} deactivated successfully.", userid);
            return "User deactivated successfully";
        } catch (Exception e) {
            log.error("Failed to deactivate user with ID {}", userid, e);
            throw new RuntimeException("Failed to deactivate user", e);
        }
    }

}