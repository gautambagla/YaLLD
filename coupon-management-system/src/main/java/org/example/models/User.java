package org.example.models;

import lombok.Getter;
import lombok.ToString;

@ToString
public class User {
    public User(Integer userId, UserType userType) {
        this.userId = userId;
        this.userType = userType;
        this.cart = new Cart(this);
    }

    @Getter private final Integer userId;

    @Getter private final UserType userType;

    @Getter private final Cart cart;
}
