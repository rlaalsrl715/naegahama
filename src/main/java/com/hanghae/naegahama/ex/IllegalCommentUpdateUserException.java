package com.hanghae.naegahama.ex;

import lombok.Getter;

@Getter
public class IllegalCommentUpdateUserException extends RuntimeException {
    public IllegalCommentUpdateUserException(String message) {
        super(message);
    }
}