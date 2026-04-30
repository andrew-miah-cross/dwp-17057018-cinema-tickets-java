package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.*;

import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketServiceTests extends TicketServiceTestHelper {

    //
    // Account Tests
    //

    @Test
    void Test_purchaseTickets_InvalidAccountId_Excepts() {
        assertThrows(
                InvalidAccountIdPurchaseException.class,
                () ->
                    ticketService.purchaseTickets(0L,
                            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
                    ),
                "All accounts with an id greater than zero are valid."
        );
    }


    @ParameterizedTest
    @CsvSource(
            nullValues = {"null"},
            value = {
                    "2,  0,  0, null",
                    "24, 0,  1, null",
                    "0, 25,  0, uk.gov.dwp.uc.pairtest.exception.AtLeastOneAdultMustBePresentPurchaseException",
                    "1, 12, 12, uk.gov.dwp.uc.pairtest.exception.InfantCountExceedsAdultsPurchaseException",
                    "0,  0,  0, uk.gov.dwp.uc.pairtest.exception.NoTicketsRequestedPurchaseException",
                    "0,  0,  1, uk.gov.dwp.uc.pairtest.exception.NoAdultOrChildPresentPurchaseException",
                    "0,  0, "+MAX_TICKETS_PER_CUSTOMER+", uk.gov.dwp.uc.pairtest.exception.NoAdultOrChildPresentPurchaseException",
            }
    )
    void Test_purchaseTickets(int numberOfAdults,int numberOfChildren,int numberOfInfants,Class expectedThrowable)
            throws
            AtLeastOneAdultMustBePresentPurchaseException,
            InfantCountExceedsAdultsPurchaseException,
            InvalidAccountIdPurchaseException,
            InvalidNumberOfTicketTypesPurchaseException,
            InvalidNumberOfTicketsPurchaseException,
            MaxTicketsPerCustomerPurchaseException,
            NoAdultOrChildPresentPurchaseException,
            NoTicketsRequestedPurchaseException,
            PaymentServiceServiceUnavailablePurchaseException,
            ReservationServiceServiceUnavailablePurchaseException
    {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, numberOfAdults),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, numberOfChildren),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, numberOfInfants)
        ).toArray(TicketTypeRequest[]::new);

        // Act / Assert.
        if( expectedThrowable == null ) {
            ticketService.purchaseTickets(1L,ticketRequests );
        } else {
            assertThrows(
                    expectedThrowable,
                    () -> ticketService.purchaseTickets(1L,ticketRequests),
                    format("Unexpected exception when attempting to buy %d adult, %d child, %d infant tickets instead of %s.",
                            numberOfAdults,numberOfChildren,numberOfInfants,
                            expectedThrowable.getCanonicalName().toString())
            );
        }
    }

}