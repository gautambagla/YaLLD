package org.example.terms.impl;

import lombok.EqualsAndHashCode;
import org.example.models.Cart;
import org.example.models.UserType;
import org.example.terms.DiscountTerm;

@EqualsAndHashCode
public class UserTypeDiscountTerm implements DiscountTerm {
    public UserTypeDiscountTerm(UserType userType) {
        this.userType = userType;
    }

    private final UserType userType;

    @Override
    public boolean isApplicable(Cart cart) {
        // UserType STANDARD < PREMIUM < VIP
        // Higher UserType can use this term's userType as well
        return this.userType.ordinal() <= cart.getUser().getUserType().ordinal();
    }
}
