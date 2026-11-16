package com.auction.application.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String username) { super("Username already exists: " + username); }
}
