package com.auction.client.util;

import javafx.application.Platform;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FxThread {
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private FxThread() {}

    public static void run(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }

    public static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    public static String formatTime(Instant instant) {
        if (instant == null) {
            return "--:--";
        }
        return TIME_FMT.format(instant);
    }
}
