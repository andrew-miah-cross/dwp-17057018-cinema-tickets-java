package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

public interface TicketPriceService {

    /***
     * Returns the price for each ticket type which may vary.
     * @param ticketType Adult, child or infant
     * @return Price in GBP
     */
    float getTicketPriceGBP(final TicketTypeRequest.Type ticketType);
}
