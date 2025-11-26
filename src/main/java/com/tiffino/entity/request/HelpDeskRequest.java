package com.tiffino.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HelpDeskRequest {

    @JsonProperty("prompt_message")
    private String promptMessage;

    @JsonProperty("history_id")
    private String historyId;

}