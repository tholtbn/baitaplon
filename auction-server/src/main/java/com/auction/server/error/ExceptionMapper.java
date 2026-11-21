package com.auction.server.error;

import com.auction.domain.exception.DomainException;
import com.google.gson.Gson;

public class ExceptionMapper {
    private static final Gson gson = new Gson();

    public static String mapException(Exception e) {
        ErrorResponse response = new ErrorResponse();
        if (e instanceof DomainException) {
            response.code = "DOMAIN_ERROR";
            response.message = e.getMessage();
        } else {
            response.code = "INTERNAL_SERVER_ERROR";
            response.message = "Đã xảy ra lỗi hệ thống nội bộ.";
            // Ghi log lỗi thực tế ở đây nhưng không lộ stack trace ra ngoài client
            e.printStackTrace();
        }
        return gson.toJson(response);
    }

    public static class ErrorResponse {
        public String code;
        public String message;
    }
}