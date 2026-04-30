package uk.gov.dwp.uc.pairtest;

public class TicketServiceTestHelper {
    protected final static int MAX_TICKETS_PER_CUSTOMER = TicketServiceImpl.MAX_TICKETS_PER_CUSTOMER;
    protected final static float ADULT_PRICE = 25.0f;
    protected final static float CHILD_PRICE = 15.0f;
    protected final static float INFANT_PRICE = 0.0f;

    protected TicketServiceImpl ticketService = new TicketServiceImpl();
}
