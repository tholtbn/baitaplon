package com.auction.domain.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auction.contract.vocab.AuctionStatus;
import com.auction.domain.exception.IllegalStateTransitionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AuctionStateMachineTest {

    @ParameterizedTest
    @CsvSource({
            "OPEN, RUNNING",
            "OPEN, CANCELED",
            "RUNNING, FINISHED",
            "RUNNING, CANCELED",
            "FINISHED, PAID",
            "FINISHED, CANCELED"
    })
    void allowsValidTransitions(AuctionStatus from, AuctionStatus to) {
        assertThat(AuctionStateMachine.canTransition(from, to)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN, FINISHED",
            "OPEN, PAID",
            "RUNNING, OPEN",
            "RUNNING, PAID",
            "FINISHED, RUNNING",
            "PAID, CANCELED",
            "PAID, OPEN",
            "CANCELED, RUNNING",
            "CANCELED, OPEN"
    })
    void rejectsInvalidTransitions(AuctionStatus from, AuctionStatus to) {
        assertThat(AuctionStateMachine.canTransition(from, to)).isFalse();
        assertThatThrownBy(() -> AuctionStateMachine.requireTransition(from, to))
                .isInstanceOf(IllegalStateTransitionException.class);
    }
}