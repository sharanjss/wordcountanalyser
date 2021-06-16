package com.code.challenge.wcanlayser.exceptionhandler;

public class DuplicateFileException extends RuntimeException {
    public DuplicateFileException(String msg){
        super(msg);
    }
}
