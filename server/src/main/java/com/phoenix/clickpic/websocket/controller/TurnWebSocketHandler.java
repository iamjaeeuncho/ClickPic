package com.phoenix.clickpic.websocket.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * packageName    : 
 * fileName       : 
 * author         : 강희원
 * date           : 2024-09-03
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-09-03        강희원             최초 생성
 */

public class TurnWebSocketHandler extends TextWebSocketHandler {

    private CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private Map<String, List<Action>> userActions = new HashMap<>(); // 사용자별 작업 기록 저장
    private int currentTurnIndex = 0;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        String sessionId = session.getId();  // 세션 ID 가져오기
        String nickname = extractNickname(session.getUri().getQuery());
        System.out.println("참여자들 닉네임 확인" + nickname);
        session.getAttributes().put("nickname", nickname);

        // 클라이언트에게 세션 ID 전송
        session.sendMessage(new TextMessage("{\"type\": \"SESSION_ID\", \"userId\": \"" + sessionId + "\"}"));

        System.out.println("WebSocket 연결 성공: " + sessionId + ", Nickname: " + nickname);
        notifyTurn();
    }

    private String extractNickname(String queryString) {
        String[] params = queryString.split("&");
        for (String param : params) {
            if (param.startsWith("nickname=")) {
                try {
                    return URLDecoder.decode(param.split("=")[1], StandardCharsets.UTF_8.toString());
                } catch (Exception e) {
                    System.err.println("닉네임 디코딩 중 오류 발생: " + e.getMessage());
                    return "Unknown";
                }
            }
        }
        return "Unknown";
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject json = new JSONObject(payload);

        // "PASS_TURN" 메시지 처리
        if ("PASS_TURN".equals(json.getString("type"))) {
            handlePassTurn(session);
        }

        // "DRAW" 메시지 처리
        if ("DRAW".equals(json.getString("type"))) {
            handleDraw(session, json);
        }

        // "STICKER" 메시지 처리
        if ("STICKER".equals(json.getString("type"))) {
            handleSticker(session, json);
        }

        // CLEAR_CANVAS 메시지 처리
        if ("CLEAR_CANVAS".equals(json.getString("type"))) {
            handleClearCanvas(session);
        }

        // UNDO_LAST 메시지 처리
        if ("UNDO_LAST".equals(json.getString("type"))) {
            handleUndoLast(session, json);
        }

        // 사진 저장 완료 메시지 처리
        if ("PHOTO_SAVED".equals(json.getString("type"))) {
            for (WebSocketSession s : sessions) {
                s.sendMessage(new TextMessage("{\"type\": \"PHOTO_SAVED\", \"imageUrl\": \"" + json.getString("imageUrl") + "\"}"));
            }
        }
    }

    private void handlePassTurn(WebSocketSession session) throws Exception {
        if (session.equals(sessions.get(currentTurnIndex))) {
            currentTurnIndex = (currentTurnIndex + 1) % sessions.size();
            notifyTurn();
        } else {
            session.sendMessage(new TextMessage("{\"type\": \"ERROR\", \"message\": \"It is not your turn!\"}"));
        }
    }

    private void handleDraw(WebSocketSession session, JSONObject json) throws Exception {
        String userId = session.getId();
        Action drawAction = new Action("DRAW", json); // Action 객체에 그리기 정보 저장
        userActions.computeIfAbsent(userId, k -> new ArrayList<>()).add(drawAction); // 사용자별 작업 저장

        for (WebSocketSession s : sessions) {
            if (!s.equals(session)) {
                s.sendMessage(new TextMessage(json.toString())); // 다른 세션에 그리기 정보 전달
            }
        }
    }

    private void handleSticker(WebSocketSession session, JSONObject json) throws Exception {
        String userId = session.getId();
        Action stickerAction = new Action("STICKER", json); // 스티커 작업도 Action에 저장
        userActions.computeIfAbsent(userId, k -> new ArrayList<>()).add(stickerAction); // 사용자별 스티커 작업 저장

        for (WebSocketSession s : sessions) {
            if (!s.equals(session)) {
                s.sendMessage(new TextMessage(json.toString())); // 다른 세션에 스티커 정보 전달
            }
        }
    }

    private void handleClearCanvas(WebSocketSession session) throws Exception {
        String userId = session.getId();
        // 해당 사용자 작업만 초기화
        userActions.put(userId, new ArrayList<>()); // 사용자별 작업 초기화

        // 전체 세션에 캔버스 초기화 메시지 전송
        for (WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage("{\"type\": \"CLEAR_CANVAS\", \"userId\": \"" + userId + "\"}"));
        }
    }


    private void handleUndoLast(WebSocketSession session, JSONObject json) throws Exception {
        String userId = session.getId();
        List<Action> actionsForUser = userActions.get(userId); // 해당 사용자의 작업 목록 가져오기

        if (actionsForUser != null && !actionsForUser.isEmpty()) {
            actionsForUser.remove(actionsForUser.size() - 1); // 마지막 작업 제거
            broadcastToAllSessions("{\"type\": \"UNDO_LAST\", \"userId\": \"" + userId + "\"}"); // 되돌리기 정보 전송
        }
    }

    private void broadcastToAllSessions(String message) throws Exception {
        for (WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage(message));
        }
    }

    private void notifyTurn() throws Exception {
        if (sessions.isEmpty()) {
            return;
        }

        WebSocketSession currentSession = sessions.get(currentTurnIndex);
        String currentNickname = getNicknameFromSession(currentSession);
        String currentSessionId = currentSession.getId();

        boolean isLastTurn = currentTurnIndex == sessions.size() - 1;

        for (WebSocketSession session : sessions) {
            boolean isTurn = session.equals(currentSession);
            session.sendMessage(new TextMessage("{\"type\": \"TURN\", \"userId\": \"" + (isTurn ? currentSessionId : "") + "\", \"nickname\": \"" + currentNickname + "\", \"isLastTurn\": " + isLastTurn + "}"));
        }
    }

    private String getNicknameFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get("nickname");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        if (!sessions.isEmpty()) {
            currentTurnIndex %= sessions.size();
            notifyTurn();
        }
    }

    // Action 클래스로 사용자 작업을 관리
    private class Action {
        private String type;
        private JSONObject data;

        public Action(String type, JSONObject data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public JSONObject getData() {
            return data;
        }
    }
}
