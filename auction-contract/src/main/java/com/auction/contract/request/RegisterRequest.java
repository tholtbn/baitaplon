package com.auction.contract.request;
import com.auction.contract.vocab.Role;

public record RegisterRequest(String username, String password, String email, Role role) {}
