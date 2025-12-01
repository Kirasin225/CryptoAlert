package com.kirasin.CryptoAlert.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {
    private boolean ok;
    private List<Result> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("update_id")
        private Long updateId;
        private Message message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private Chat chat;
        private String text;
        private From from;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chat {
        private Long id;
        @JsonProperty("username")
        private String username;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class From {
        @JsonProperty("username")
        private String username;
    }
}
