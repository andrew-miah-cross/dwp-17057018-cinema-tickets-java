package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.PaymentServiceServiceUnavailablePurchaseException;
import uk.gov.dwp.uc.pairtest.exception.ReservationServiceServiceUnavailablePurchaseException;

import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class TicketServiceTestsPurchasing extends TicketServiceTestHelper {

    //
    // Purchasing Tests
    //

    @Test
    void Test_purchaseTicket_PriceIsCorrect_1Adult() {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        ).toArray(TicketTypeRequest[]::new);


        // Act
        var gbp = ticketService.getCostGBP(ticketRequests);

        // Assert.
        assertEquals(ADULT_PRICE, gbp, format("Price for 1 adult ticket should be %f", ADULT_PRICE));
    }


    @Test
    void Test_purchaseTicket_PriceIsCorrect_1Adult_1Child_1Infant() {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        ).toArray(TicketTypeRequest[]::new);

        // Act
        var gbp = ticketService.getCostGBP(ticketRequests);

        // Assert.
        assertEquals(
                ADULT_PRICE + CHILD_PRICE + INFANT_PRICE,
                gbp, format("Price for 1 adult, child and infant ticket should be %f", ADULT_PRICE + CHILD_PRICE + INFANT_PRICE)
        );
    }

    @Test
    void Test_purchaseTicket_PaymentServiceDown_Excepts() {
        // Arrange.
        TicketPaymentServiceImpl mockTicketPaymentServiceImpl = mock(TicketPaymentServiceImpl.class);
        doThrow(new RuntimeException("PaymentService Unavailable"))
                .when(mockTicketPaymentServiceImpl).makePayment(anyLong(), anyInt());
        ticketService.ticketPaymentService = mockTicketPaymentServiceImpl;

        // Act / Assert.
        assertThrows(
                PaymentServiceServiceUnavailablePurchaseException.class,
                () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)),
                "Purchase service must inform api-consumer of payment service connection issue, to surface the error with the actor/stake holder."
        );
    }

    @Test
    void Test_purchaseTicket_ReservationServiceDown_Excepts() {
        // Arrange.
        SeatReservationServiceImpl mockSeatReservationServiceImpl = mock(SeatReservationServiceImpl.class);
        doThrow(new RuntimeException("ReservationService Unavailable"))
                .when(mockSeatReservationServiceImpl).reserveSeat(anyLong(), anyInt());
        ticketService.seatReservationService = mockSeatReservationServiceImpl;

        // Act / Assert.
        assertThrows(
                ReservationServiceServiceUnavailablePurchaseException.class,
                () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)),
                "Purchase service must inform api-consumer of service of connection issue, to surface the error with the actor/stake holder."
        );

    }

}
