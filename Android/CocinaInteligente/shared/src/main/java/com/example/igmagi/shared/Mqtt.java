package com.example.igmagi.shared;

import java.util.UUID;

public class Mqtt {
    public static final String TAG = "MQTT";
    public static final String topicRoot="grupo01/cocinainteligente/";
    public static final int qos = 1;
    public static final String broker = "tcp://broker.hivemq.com:1883";
    public static final String clientId = UUID.randomUUID().toString();
    public static final String serverId = "329ee43108b44e82a3030cb195c29857";
    public static final String weight = "weight";
    public static final String presence = "presence";
    public static final String playingRecipe = "playingRecipe";
    public static final String listenChannel = "listenChannel";
}