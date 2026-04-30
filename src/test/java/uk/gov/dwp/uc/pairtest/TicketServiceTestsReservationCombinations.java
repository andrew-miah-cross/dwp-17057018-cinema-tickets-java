package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.*;

import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TicketServiceTestsReservationCombinations extends TicketServiceTestHelper {

    //
    // TicketTypeRequest tests
    //

    @Test
    void Test_purchaseTickets_MaxThreeTypes_Excepts() {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        ).toArray(TicketTypeRequest[]::new);

        // Act / Assert.
        assertThrows(
                InvalidNumberOfTicketTypesPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketRequests),
                "Only three ticket type requests permitted."
        );
    }

    @Test
    void Test_purchaseTickets_TicketTypesNotConsolidated_Excepts() {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        ).toArray(TicketTypeRequest[]::new);

        // Act / Assert.
        assertThrows(
                InvalidNumberOfTicketTypesPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketRequests),
                "Quantities for each ticket type must be consolidated."
        );
    }

    @Test
    void Test_purchaseTickets_TicketTypeParameter_QuantityNegative_Excepts() {
        assertThrows(
                InvalidNumberOfTicketsPurchaseException.class,
                () -> ticketService.purchaseTickets(1L,
                            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1)
                    ),
                "Number of tickets cannot be negative."
        );
    }

    @Test
    void Test_purchaseTickets_MoreInfantsThanAdults_Excepts() {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        ).toArray(TicketTypeRequest[]::new);

        // Act/Assert.
        assertThrows(
                InfantCountExceedsAdultsPurchaseException.class,
                () -> ticketService.purchaseTickets(1L,ticketRequests ),
                "Cannot purchase more infant tickets than adult tickets: one infant per adult lap"
        );
    }


    @Test
    void Test_purchaseTickets_MaxPerCustomer_Excepts() {
        // Act / Assert.
        assertThrows(
                MaxTicketsPerCustomerPurchaseException.class,
                () -> ticketService.purchaseTickets(
                            1L,
                            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, MAX_TICKETS_PER_CUSTOMER + 1)
                    ),
                format("Only a maximum of %d tickets that can be purchased at a time.", MAX_TICKETS_PER_CUSTOMER)
        );
    }

    @Test
    void Test_purchaseTickets_MaxPerCustomer_TwoTypes_Excepts() {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, MAX_TICKETS_PER_CUSTOMER),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, MAX_TICKETS_PER_CUSTOMER)
        ).toArray(TicketTypeRequest[]::new);

        // Act / Assert.
        assertThrows(
                MaxTicketsPerCustomerPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketRequests),
                format("Only a maximum of %d tickets that can be purchased at a time.", MAX_TICKETS_PER_CUSTOMER)
        );
    }

    @Test
    void Test_purchaseTickets_AtLeastOneAdultPresent_Excepts() {
        // Arrange.
        TicketTypeRequest[] ticketRequests = Stream.of(
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, MAX_TICKETS_PER_CUSTOMER)
        ).toArray(TicketTypeRequest[]::new);

        // Act / Assert.
        assertThrows(
                AtLeastOneAdultMustBePresentPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, ticketRequests),
                "At least one adult must be present"
        );
    }


}
