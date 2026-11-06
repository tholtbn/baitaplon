// dto/UserDto.java
package com.auction.contract.dto;

import com.auction.contract.vocab.Role;

public record UserDto(String id, String username, String email, Role role) {}