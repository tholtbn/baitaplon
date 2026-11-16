package com.auction.application.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String resource, String id) { super(resource + " not found: " + id); }
}
