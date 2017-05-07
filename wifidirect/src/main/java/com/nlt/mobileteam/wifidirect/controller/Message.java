package com.nlt.mobileteam.wifidirect.controller;

public enum Message {

    MESSAGE_PROJ_INFO,
    MESSAGE_ASSISTANT_DISCONNECTING,
    MESSAGE_DIRECTOR_DISCONNECTING,
    MESSAGE_REQUEST_VIDEO,
    MESSAGE_HEADER_SENDING_VIDEO,
    MESSAGE_GET_NEXT_VIDEO_PART,
    MESSAGE_ABORT_VIDEO_SENDING,
    MESSAGE_PART_OF_VIDEO_SENDING,

    MESSAGE_MEDIA_FORMAT,
    MESSAGE_START_PEER_BROADCAST_SERVICE,
    MESSAGE_STOP_PEER_BROADCAST_SERVICE,
    MESSAGE_INIT_MEDIA_SOCKETS,
    MESSAGE_PING,
    MESSAGE_PONG;


    public static Message valueOfOrdinal(int ordinal) {
        return Message.values()[ordinal];
    }

    public static Message valueOfOrdinal(String ordinal) {
        return valueOfOrdinal(Integer.parseInt(ordinal));
    }

    @Override
    public String toString() {
        return String.valueOf(ordinal());
    }
}
