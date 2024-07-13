package com.accommodation_management_booking.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetLink);
    void sendRegistrationEmail(String email, String content);
    void sendCompleteRegistrationEmail(String email, String content);
    void sendBill(String toEmail, String subject, String body);

    void sendMailGuess(String email, String content);
}
