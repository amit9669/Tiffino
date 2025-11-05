package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotResponse {

    private String reply;
    private String action;
    private Map<String, Object> details;
    private List<String> quickReplies;

    public static BotResponse of(String reply, String action) {
        BotResponse b = new BotResponse();
        b.reply = reply;
        b.action = action;
        return b;
    }

    public static BotResponse error(String msg) {
        BotResponse b = new BotResponse();
        b.reply = msg;
        b.action = "error";
        return b;
    }
}
