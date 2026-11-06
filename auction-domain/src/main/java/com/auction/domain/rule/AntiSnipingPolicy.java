package com.auction.domain.rule;

import com.auction.domain.exception.ValidationException;
import java.time.Duration;
import java.time.Instant;

/**
 * Luật gia hạn chống "bid phút chót": nếu có bid khi còn không quá {@code window} thì kéo dài
 * thời điểm kết thúc thêm {@code extension}.
 */
public record AntiSnipingPolicy(Duration window, Duration extension) {

    public AntiSnipingPolicy {
        if (window == null || extension == null) {
            throw new ValidationException("window and extension must not be null");
        }
        if (window.isNegative() || extension.isNegative()) {
            throw new ValidationException("window and extension must not be negative");
        }
    }

    /** Không gia hạn bao giờ. */
    public static AntiSnipingPolicy disabled() {
        return new AntiSnipingPolicy(Duration.ZERO, Duration.ZERO);
    }

    /** Trả về thời điểm kết thúc mới (bằng {@code endTime} nếu không cần gia hạn). */
    public Instant extend(Instant now, Instant endTime) {
        if (extension.isZero()) {
            return endTime;
        }
        Duration remaining = Duration.between(now, endTime);
        return remaining.compareTo(window) <= 0 ? endTime.plus(extension) : endTime;
    }
}