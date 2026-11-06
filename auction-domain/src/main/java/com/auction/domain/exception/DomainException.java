package com.auction.domain.exception;

/** Gốc của mọi lỗi vi phạm luật nghiệp vụ. Tầng trên dùng lớp này để map sang mã lỗi. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}