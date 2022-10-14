package com.qiniu.qvs;

import com.qiniu.qvs.model.PlayContral;
import com.qiniu.qvs.model.VoiceChat;
import com.qiniu.util.StringMap;

public class QvsMap {
    private QvsMap() {
    }

    public static StringMap getVoiceChatMap(VoiceChat voiceChat) {
        StringMap params = new StringMap().putNotNull("isV2", voiceChat.getLatency());
        params.put("channels", voiceChat.getChannels());
        params.put("version", voiceChat.getVersion());
        params.put("transProtocol", voiceChat.getTransProtocol());
        return params;
    }

    public static StringMap getPlayContralMap(PlayContral playContral) {
        StringMap params = new StringMap().putNotNull("command", playContral.getCommand());
        params.put("range", playContral.getRange());
        params.put("scale", playContral.getScale());
        return params;
    }
}
