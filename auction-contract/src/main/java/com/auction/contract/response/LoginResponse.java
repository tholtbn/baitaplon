// response/LoginResponse.java
package com.auction.contract.response;

import com.auction.contract.dto.UserDto;

public record LoginResponse(String token, UserDto user) {}