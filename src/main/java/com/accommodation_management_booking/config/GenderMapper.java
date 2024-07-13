package com.accommodation_management_booking.config;

import com.accommodation_management_booking.entity.Dorm;
import com.accommodation_management_booking.entity.User;

public class GenderMapper {
    public static Dorm.DormGender map(User.Gender userGender) {
        switch (userGender) {
            case Male:
                return Dorm.DormGender.Male;
            case Female:
                return Dorm.DormGender.Female;
            case Other:
                return Dorm.DormGender.Other;
            default:
                throw new IllegalArgumentException("Unknown gender: " + userGender);
        }
    }
}
