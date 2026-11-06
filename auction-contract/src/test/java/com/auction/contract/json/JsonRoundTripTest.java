package com.auction.contract.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.auction.contract.protocol.MessageType;
import com.auction.contract.protocol.Request;
import com.auction.contract.request.PlaceBidRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class JsonRoundTripTest {

    @Test
    void requestSurvivesSerializationAndKeepsBigDecimalExact() throws Exception {
        Request original = Request.of(MessageType.PLACE_BID, "tok",
                new PlaceBidRequest("a1", new BigDecimal("1234.50")));

        String line = Json.mapper().writeValueAsString(original);
        Request parsed = Json.mapper().readValue(line, Request.class);

        assertThat(line).doesNotContain("\n");
        assertThat(parsed.type()).isEqualTo(MessageType.PLACE_BID);
        assertThat(parsed.payloadAs(PlaceBidRequest.class).amount())
                .isEqualByComparingTo("1234.50");
    }
}