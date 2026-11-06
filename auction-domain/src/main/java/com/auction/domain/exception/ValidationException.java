package com.auction.domain.exception;

/** Dữ liệu đầu vào không hợp lệ (rỗng, âm, sai khoảng thời gian...). */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}