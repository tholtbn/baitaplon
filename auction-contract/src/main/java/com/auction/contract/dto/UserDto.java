package com.auction.application.dto;

import com.auction.contract.vocab.Role;
public record UserDto(String id, String username, String email, Role role) {}