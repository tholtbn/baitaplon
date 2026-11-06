package com.auction.domain.model;

import com.auction.domain.exception.ValidationException;
import java.math.BigDecimal;

/** Các hàm kiểm tra điều kiện đầu vào dùng chung cho entity (tránh lặp code validate). */
public final class Guard {

    private Guard() {}

    /** Trả về chính {@code value} nếu không null, ngược lại ném {@link ValidationException}. */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null");
        }
        return value;
    }

    /** Trả về chính {@code value} nếu không null và không rỗng/toàn khoảng trắng. */
    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value;
    }

    /** Trả về chính {@code value} nếu lớn hơn 0. */
    public static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero");
        }
        return value;
    }

    /** Trả về chính {@code value} nếu {@code value >= minimum}. */
    public static int requireAtLeast(int value, int minimum, String fieldName) {
        if (value < minimum) {
            throw new ValidationException(fieldName + " must be at least " + minimum);
        }
        return value;
    }
}