package com.auction.contract.request;
public record LoginResponse(String token, UserDto user) {}