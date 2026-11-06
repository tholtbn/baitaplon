package com.auction.contract.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;

/** ObjectMapper dùng chung cho client và server để hai bên luôn hiểu giống nhau. */
public final class Json {

    private static final ObjectMapper MAPPER = create();

    private Json() {}

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    private static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);   // ISO-8601
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configOverride(BigDecimal.class)                           // tiền là string
                .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        return mapper;
    }
}