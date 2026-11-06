package com.auction.contract.request;

public record RegisterRequest(String username, String password, String email, Role role) {}
