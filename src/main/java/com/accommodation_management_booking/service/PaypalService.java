package com.accommodation_management_booking.service;

import com.accommodation_management_booking.config.PaypalPaymentIntent;
import com.accommodation_management_booking.config.PaypalPaymentMethod;
import com.accommodation_management_booking.repository.PaymentRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaypalService {
    @Autowired
    private APIContext apiContext;

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPayment(
        float total,
        String currency,
        PaypalPaymentMethod method,
        PaypalPaymentIntent intent,
        String description,
        String cancelUrl,
        String successUrl) throws PayPalRESTException {
            Amount amount = new Amount();
            amount.setCurrency(currency);
            amount.setTotal(String.format("%.2f", total));

            Transaction transaction = new Transaction();
            transaction.setDescription(description);
            transaction.setAmount(amount);

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod(method.toString());

            Payment payment = new Payment();
            payment.setIntent(intent.toString());
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(cancelUrl);
            redirectUrls.setReturnUrl(successUrl);
            payment.setRedirectUrls(redirectUrls);

            apiContext.setMaskRequestId(true);
            return payment = payment.create(apiContext);
        }

        public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
            Payment payment = new Payment();
            payment.setId(paymentId);
            PaymentExecution paymentExecution=new PaymentExecution();
            paymentExecution.setPayerId(payerId);
            return payment.execute(apiContext, paymentExecution);
        }
    public void refundPayment(String paymentId, float amount) throws PayPalRESTException {
        try {
            // Create a refund request
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setAmount(new Amount().setCurrency("USD").setTotal(String.format("%.2f", amount)));

            // Get the payment and find the sale transaction
            Payment payment = Payment.get(apiContext, paymentId);
            Transaction transaction = payment.getTransactions().get(0); // Assuming there's only one transaction
            RelatedResources relatedResources = transaction.getRelatedResources().get(0); // Assuming there's only one related resource
            Sale sale = relatedResources.getSale();

            // Create the refund
            DetailedRefund refund = sale.refund(apiContext, refundRequest);

            System.out.println("Refund ID: " + refund.getId());
        } catch (PayPalRESTException e) {
            System.err.println(e.getDetails());
            throw e;
        }
    }

    public List<com.accommodation_management_booking.entity.Payment> getPaymentsByMethod(com.accommodation_management_booking.entity.Payment.PaymentMethod method) {
        return paymentRepository.findByPaymentMethod(method);
    }

}
