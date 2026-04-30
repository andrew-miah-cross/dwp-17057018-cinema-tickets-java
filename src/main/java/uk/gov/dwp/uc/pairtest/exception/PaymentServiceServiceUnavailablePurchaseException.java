package uk.gov.dwp.uc.pairtest.exception;

public class PaymentServiceServiceUnavailablePurchaseException extends InvalidPurchaseException {
    public PaymentServiceServiceUnavailablePurchaseException(Throwable ex) {
        super(ex);
    }
}
