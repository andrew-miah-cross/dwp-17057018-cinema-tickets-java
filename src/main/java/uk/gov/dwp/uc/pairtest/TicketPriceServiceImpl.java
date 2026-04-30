package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidTicketTypePriceException;

import java.util.HashMap;
import java.util.Map;

public class TicketPriceServiceImpl implements  TicketPriceService{

    protected static final HashMap<TicketTypeRequest.Type,Float> ticketTypePriceGBPMap = new HashMap<>(Map.of(
            TicketTypeRequest.Type.ADULT,25.0f,
            TicketTypeRequest.Type.CHILD,15.0f,
            TicketTypeRequest.Type.INFANT,0.0f
    ));

    @Override
    public float getTicketPriceGBP(TicketTypeRequest.Type ticketType) {
        // Omitting real implementation, though in practice price must be reconfigurable without rebuild.
        return switch (ticketType) {
            case ADULT, CHILD, INFANT -> ticketTypePriceGBPMap.get(ticketType);
            // Allow for further categories, e.g. disabled, blind, matinée etc.
            default -> throw new InvalidTicketTypePriceException();
        };
    }
}
