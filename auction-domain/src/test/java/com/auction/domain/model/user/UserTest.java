package com.auction.domain.model.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auction.contract.vocab.Role;
import com.auction.domain.exception.ValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void eachSubclassReportsItsOwnRole() {
        List<User> users = List.of(
                new Bidder("1", "alice", "hash", "a@x.com"),
                new Seller("2", "bob", "hash", "b@x.com"),
                new Admin("3", "carol", "hash", "c@x.com"));

        assertThat(users).extracting(User::getRole)
                .containsExactly(Role.BIDDER, Role.SELLER, Role.ADMIN);
    }

    @Test
    void printInfoIncludesRoleAndUsername() {
        User user = new Seller("2", "bob", "hash", "b@x.com");

        assertThat(user.printInfo()).contains("SELLER").contains("bob");
    }

    @Test
    void rejectsBlankUsername() {
        assertThatThrownBy(() -> new Bidder("1", " ", "hash", "a@x.com"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void usersWithSameIdAndClassAreEqual() {
        assertThat(new Bidder("1", "alice", "h1", "a@x.com"))
                .isEqualTo(new Bidder("1", "other", "h2", "o@x.com"));
    }
}