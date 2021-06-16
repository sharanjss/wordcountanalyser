package com.code.challenge.wcanlayser.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseMessage {

    /**
     * current timestamp
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Messages
     */
    private String message;


    public ResponseMessage(String message) {
        this.message = message;
    }

}
