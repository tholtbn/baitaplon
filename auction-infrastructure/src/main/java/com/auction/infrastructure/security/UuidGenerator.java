package com.auction.infrastructure.security;

import com.auction.application.port.IdGenerator;
import java.util.UUID;

public class UuidGenerator implements IdGenerator {
    @Override
    public String newId() {
        return UUID.randomUUID().toString();
    }
}