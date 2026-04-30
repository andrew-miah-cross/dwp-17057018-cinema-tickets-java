package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.*;

import java.util.Arrays;

public class TicketServiceImpl implements TicketService {
    protected final static int MAX_TICKETS_PER_CUSTOMER = 25;

    // Spring et al. omitted.
    TicketPriceService ticketPriceService = new TicketPriceServiceImpl();
    TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
    SeatReservationService seatReservationService = new SeatReservationServiceImpl();

    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, final TicketTypeRequest... ticketTypeRequests)
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

        validateAccountId(accountId);
        validateTicketQuantities(ticketTypeRequests);
        validateTicketTypes(ticketTypeRequests);

        int totalSeatsToReserve = evaluateTotalSeatToReserve(ticketTypeRequests);

        enforceRuleAtLeastOneAdultPresent(ticketTypeRequests);
        enforceRuleOneInfantPerAdult(ticketTypeRequests);
        enforceRuleMaxTicketsPerCustomer(ticketTypeRequests);

        // Make payment.
        try {

            int priceGBP = (int)Math.ceil( getCostGBP(ticketTypeRequests) );
            ticketPaymentService.makePayment( accountId,priceGBP );

        } catch( Throwable ex ) {
            throw new PaymentServiceServiceUnavailablePurchaseException(ex);
        }

        // Reserve seats.
        try {

            seatReservationService.reserveSeat(accountId,totalSeatsToReserve);

        } catch( Throwable ex ) {
            throw new ReservationServiceServiceUnavailablePurchaseException(ex);
        }

    }

    private void enforceRuleAtLeastOneAdultPresent(final TicketTypeRequest... ticketTypeRequests) throws AtLeastOneAdultMustBePresentPurchaseException {
        var countOfAdults = Arrays.stream(ticketTypeRequests)
                .filter(el -> el.getTicketType() == TicketTypeRequest.Type.ADULT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        if( countOfAdults == 0 ) {
            throw new AtLeastOneAdultMustBePresentPurchaseException();
        }
    }

    private int evaluateTotalSeatToReserve(TicketTypeRequest[] ticketTypeRequests) throws NoAdultOrChildPresentPurchaseException, NoTicketsRequestedPurchaseException {
        var countOfAdultOrChildSeatsRequired = Arrays.stream(ticketTypeRequests)
                .filter(el -> el.getTicketType() == TicketTypeRequest.Type.ADULT || el.getTicketType() == TicketTypeRequest.Type.CHILD)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
        var countOfAllCustomers = Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        if( countOfAllCustomers == 0 ) {
            throw new NoTicketsRequestedPurchaseException();
        }

        if( countOfAdultOrChildSeatsRequired == 0 ) {
            throw new NoAdultOrChildPresentPurchaseException();
        }

        return countOfAdultOrChildSeatsRequired;
    }

    private void validateTicketTypes(final TicketTypeRequest... ticketTypeRequests) throws InvalidNumberOfTicketTypesPurchaseException {

        var countOfAdultTypes = Arrays.stream(ticketTypeRequests)
                .filter(el -> el.getTicketType() == TicketTypeRequest.Type.ADULT)
                .count();

        var countOfChildTypes = Arrays.stream(ticketTypeRequests)
                .filter(el -> el.getTicketType() == TicketTypeRequest.Type.CHILD)
                .count();

        var countOfInfantTypes = Arrays.stream(ticketTypeRequests)
                .filter(el -> el.getTicketType() == TicketTypeRequest.Type.INFANT)
                .count();

        if( countOfAdultTypes > 1 || countOfChildTypes > 1 || countOfInfantTypes > 1 ) {
            throw new InvalidNumberOfTicketTypesPurchaseException();
        }

    }

    private void validateTicketQuantities(final TicketTypeRequest... ticketTypeRequests) throws InvalidNumberOfTicketsPurchaseException {
        for( var ticket : ticketTypeRequests ) {
            if( ticket.getNoOfTickets() < 0 ) {
                throw new InvalidNumberOfTicketsPurchaseException();
            }
        }
    }

    private void validateAccountId(Long accountId) throws InvalidAccountIdPurchaseException {
        if( accountId < 1L ) {
            throw new InvalidAccountIdPurchaseException();
        }
    }

    void enforceRuleOneInfantPerAdult(final TicketTypeRequest... ticketTypeRequests) throws InfantCountExceedsAdultsPurchaseException
    {
        // There must be at least or more adults than infants for one infant per lap.
        var countOfAdults = Arrays.stream(ticketTypeRequests)
                .filter(el -> el.getTicketType() == TicketTypeRequest.Type.ADULT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        var countOfInfants = Arrays.stream(ticketTypeRequests)
                .filter(el -> el.getTicketType() == TicketTypeRequest.Type.INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        if (countOfAdults < countOfInfants) {
            throw new InfantCountExceedsAdultsPurchaseException();
        }
    }

    void enforceRuleMaxTicketsPerCustomer(final TicketTypeRequest... ticketTypeRequests) throws MaxTicketsPerCustomerPurchaseException {

        var countOfAllTickets = Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        if( countOfAllTickets > MAX_TICKETS_PER_CUSTOMER ) {
            throw new MaxTicketsPerCustomerPurchaseException();
        }

    }


    /*
     * AMC: I've allowed this method protected access for unit-testing.
     */
    protected float getCostGBP(final TicketTypeRequest... ticketTypeRequests) {
        float totalCostGBP = 0.0f;

        for( var ticketRequest : ticketTypeRequests ) {
            final float ticketPrice = ticketPriceService.getTicketPriceGBP(ticketRequest.getTicketType());
            totalCostGBP += ticketRequest.getNoOfTickets() * ticketPrice;
        }

        return totalCostGBP;
    }
}