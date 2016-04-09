package com.nearce.chatcraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public abstract class WebSocketHandler {
    private Map<InetSocketAddress, WebSocket> activeSockets = new HashMap<>();
    private Map<InetSocketAddress, ChatParticipant> participantMap = new HashMap<>();

    private WebSocketServer server = new WebSocketServer(new InetSocketAddress(8080)) {
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            activeSockets.put(conn.getRemoteSocketAddress(), conn);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            leave(conn.getRemoteSocketAddress(), new JsonObject());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            process(conn.getRemoteSocketAddress(), message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {

        }
    };

    public WebSocketHandler() throws UnknownHostException {
    }

    public void start() {
        server.start();
    }

    public void stop() throws IOException, InterruptedException {
        server.stop();
    }

    private void process(InetSocketAddress address, String message) {
        JsonObject object = new JsonParser().parse(message).getAsJsonObject();

        UUID identifier = UUID.fromString(object.getAsJsonPrimitive("user").getAsString());
        String method = object.getAsJsonPrimitive("method").getAsString();
        JsonObject params = object.getAsJsonObject("params");

        switch (method) {
            case "join":
                join(address, identifier, params);
                break;
            case "leave":
                leave(address, params);
                break;
            case "send":
                sendMessage(address, params);
                break;
        }
    }

    private void broadcast(String message) {
        for (WebSocket webSocket : activeSockets.values()) {
            webSocket.send(message);
        }
    }

    private void join(InetSocketAddress address, UUID identifier, JsonObject params) {
        ChatParticipant participant = new ChatParticipant(identifier, params.getAsJsonPrimitive("name").getAsString() + "*");
        participantMap.put(address, participant);

        clientJoin(participant, true);
        remoteClientJoin(participant);
    }

    private void leave(InetSocketAddress address, JsonObject params) {
        if (activeSockets.remove(address) != null) {
            ChatParticipant participant = participantMap.remove(address);
            if (participant != null) {
                clientLeave(participant, true);
                remoteClientLeave(participant);
            }
        }
    }

    private void sendMessage(InetSocketAddress address, JsonObject params) {
        ChatParticipant participant = participantMap.get(address);
        if (participant != null) {
            String message = params.getAsJsonPrimitive("message").getAsString();
            clientSendMessage(participant, message);

            remoteClientSendMessage(new ChatMessage(participant, message));
        }
    }

    public void clientJoin(ChatParticipant participant) {
        clientJoin(participant, false);
    }

    private void clientJoin(ChatParticipant participant, boolean remote) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("name", participant.getName());
        requestParams.addProperty("remote", remote);

        JsonObject request = new JsonObject();
        request.addProperty("method", "join");
        request.add("params", requestParams);

        broadcast(request.toString());
    }

    public void clientLeave(ChatParticipant participant) {
        clientLeave(participant, false);
    }

    private void clientLeave(ChatParticipant participant, boolean remote) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("name", participant.getName());
        requestParams.addProperty("remote", remote);

        JsonObject request = new JsonObject();
        request.addProperty("method", "leave");
        request.add("params", requestParams);

        broadcast(request.toString());
    }

    public void clientSendMessage(ChatParticipant participant, String message) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("sender", participant.getName());
        requestParams.addProperty("message", message);

        JsonObject request = new JsonObject();
        request.addProperty("method", "send");
        request.add("params", requestParams);

        broadcast(request.toString());
    }

    public abstract void remoteClientJoin(ChatParticipant client);

    public abstract void remoteClientLeave(ChatParticipant client);

    public abstract void remoteClientSendMessage(ChatMessage message);
}
