package com.mobenga.hm.openbet.service.stub;

import com.mobenga.hm.openbet.dto.MonitorCriteria;
import com.mobenga.hm.openbet.dto.MonitorOperation;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub realization of operations storage
 */
public class OpenbetOperationsManipulationServiceStub implements OpenbetOperationsManipulationService {
    private List<MonitorOperation> operations = new ArrayList<>();
    private List<String> types = new ArrayList<>();

    @PostConstruct
    public void initStorage(){
        operations.add(operation("2016-02-29 11:57", "cashoutBet", "Jimmi1", "O/7523244/0007271", 1200, "Success",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBet adminMode=\"N\" xmlns=\"http://schema.products.sportsbook.openbet.com/bet\"> <customerRef id=\"108993\"/> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <channelRef id=\"M\"/> <cashoutValue amount=\"0.67\"> <currencyRef id=\"GBP\"/> </cashoutValue> </cashoutBet>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBetResponse xmlns=\"http://schema.products.sportsbook.openbet.com/bet\" xmlns:ns2=\"http://schema.products.sportsbook.openbet.com/betcommon\"> <betError code=\"BET_ERROR\" subErrorCode=\"CASHOUT_PENDING\"> <errorDesc>CASHOUT_PENDING</errorDesc> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <cashoutDelay>7</cashoutDelay> <cashoutBetDelayId>18357</cashoutBetDelayId> </betError> <bet receipt=\"O/0108993/0003202\" isSettled=\"N\" isFunded=\"Y\" isConfirmed=\"Y\" timeStamp=\"2016-02-17T15:13:23.000Z\" id=\"129528\" provider=\"OpenBetSports\" addr=\"10.99.30.13\"> <betTypeRef id=\"SGL\"/> <stake amount=\"1.00\" freebet=\"0.00\" stakePerLine=\"1.00\"> <currencyRef id=\"GBP\"/> </stake> <payout potential=\"7.00\" winnings=\"0.00\" refunds=\"0.00\" bonus=\"0.0\"/> <lines number=\"1\" win=\"0\" lose=\"0\" void=\"0\"/> <cashoutValue amount=\"0.67\"/> <partialCashout available=\"Y\"/> <leg result=\"-\" documentId=\"1\"> <ns2:sportsLeg> <ns2:price num=\"6\" den=\"1\" legType=\"-\"> <ns2:priceTypeRef id=\"LP\"/> </ns2:price> <ns2:placeTerms/> <ns2:outcomeCombiRef/> <ns2:winPlaceRef id=\"WIN\"/> <ns2:legPart> <ns2:outcomeRef id=\"136410343\"/> </ns2:legPart> </ns2:sportsLeg> </leg> </bet> </cashoutBetResponse>",
                ""));
        operations.add(operation("2016-02-29 11:57", "cashoutBet", "Jimmi12", "O/7523244/0007271", 1100, "Success",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBet adminMode=\"N\" xmlns=\"http://schema.products.sportsbook.openbet.com/bet\"> <customerRef id=\"108993\"/> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <channelRef id=\"M\"/> <cashoutValue amount=\"0.67\"> <currencyRef id=\"GBP\"/> </cashoutValue> </cashoutBet>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBetResponse xmlns=\"http://schema.products.sportsbook.openbet.com/bet\" xmlns:ns2=\"http://schema.products.sportsbook.openbet.com/betcommon\"> <betError code=\"BET_ERROR\" subErrorCode=\"CASHOUT_PENDING\"> <errorDesc>CASHOUT_PENDING</errorDesc> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <cashoutDelay>7</cashoutDelay> <cashoutBetDelayId>18357</cashoutBetDelayId> </betError> <bet receipt=\"O/0108993/0003202\" isSettled=\"N\" isFunded=\"Y\" isConfirmed=\"Y\" timeStamp=\"2016-02-17T15:13:23.000Z\" id=\"129528\" provider=\"OpenBetSports\" addr=\"10.99.30.13\"> <betTypeRef id=\"SGL\"/> <stake amount=\"1.00\" freebet=\"0.00\" stakePerLine=\"1.00\"> <currencyRef id=\"GBP\"/> </stake> <payout potential=\"7.00\" winnings=\"0.00\" refunds=\"0.00\" bonus=\"0.0\"/> <lines number=\"1\" win=\"0\" lose=\"0\" void=\"0\"/> <cashoutValue amount=\"0.67\"/> <partialCashout available=\"Y\"/> <leg result=\"-\" documentId=\"1\"> <ns2:sportsLeg> <ns2:price num=\"6\" den=\"1\" legType=\"-\"> <ns2:priceTypeRef id=\"LP\"/> </ns2:price> <ns2:placeTerms/> <ns2:outcomeCombiRef/> <ns2:winPlaceRef id=\"WIN\"/> <ns2:legPart> <ns2:outcomeRef id=\"136410343\"/> </ns2:legPart> </ns2:sportsLeg> </leg> </bet> </cashoutBetResponse>",
                ""));
        operations.add(operation("2016-02-29 11:58", "cashoutBet", "Jimmi13", "O/7523244/0007271", 1300, "Success",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBet adminMode=\"N\" xmlns=\"http://schema.products.sportsbook.openbet.com/bet\"> <customerRef id=\"108993\"/> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <channelRef id=\"M\"/> <cashoutValue amount=\"0.67\"> <currencyRef id=\"GBP\"/> </cashoutValue> </cashoutBet>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBetResponse xmlns=\"http://schema.products.sportsbook.openbet.com/bet\" xmlns:ns2=\"http://schema.products.sportsbook.openbet.com/betcommon\"> <betError code=\"BET_ERROR\" subErrorCode=\"CASHOUT_PENDING\"> <errorDesc>CASHOUT_PENDING</errorDesc> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <cashoutDelay>7</cashoutDelay> <cashoutBetDelayId>18357</cashoutBetDelayId> </betError> <bet receipt=\"O/0108993/0003202\" isSettled=\"N\" isFunded=\"Y\" isConfirmed=\"Y\" timeStamp=\"2016-02-17T15:13:23.000Z\" id=\"129528\" provider=\"OpenBetSports\" addr=\"10.99.30.13\"> <betTypeRef id=\"SGL\"/> <stake amount=\"1.00\" freebet=\"0.00\" stakePerLine=\"1.00\"> <currencyRef id=\"GBP\"/> </stake> <payout potential=\"7.00\" winnings=\"0.00\" refunds=\"0.00\" bonus=\"0.0\"/> <lines number=\"1\" win=\"0\" lose=\"0\" void=\"0\"/> <cashoutValue amount=\"0.67\"/> <partialCashout available=\"Y\"/> <leg result=\"-\" documentId=\"1\"> <ns2:sportsLeg> <ns2:price num=\"6\" den=\"1\" legType=\"-\"> <ns2:priceTypeRef id=\"LP\"/> </ns2:price> <ns2:placeTerms/> <ns2:outcomeCombiRef/> <ns2:winPlaceRef id=\"WIN\"/> <ns2:legPart> <ns2:outcomeRef id=\"136410343\"/> </ns2:legPart> </ns2:sportsLeg> </leg> </bet> </cashoutBetResponse>",
                ""));
        operations.add(operation("2016-02-29 11:58", "cashoutBet", "Jimmi14", "O/7523244/0007271", 1700, "Success",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBet adminMode=\"N\" xmlns=\"http://schema.products.sportsbook.openbet.com/bet\"> <customerRef id=\"108993\"/> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <channelRef id=\"M\"/> <cashoutValue amount=\"0.67\"> <currencyRef id=\"GBP\"/> </cashoutValue> </cashoutBet>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBetResponse xmlns=\"http://schema.products.sportsbook.openbet.com/bet\" xmlns:ns2=\"http://schema.products.sportsbook.openbet.com/betcommon\"> <betError code=\"BET_ERROR\" subErrorCode=\"CASHOUT_PENDING\"> <errorDesc>CASHOUT_PENDING</errorDesc> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <cashoutDelay>7</cashoutDelay> <cashoutBetDelayId>18357</cashoutBetDelayId> </betError> <bet receipt=\"O/0108993/0003202\" isSettled=\"N\" isFunded=\"Y\" isConfirmed=\"Y\" timeStamp=\"2016-02-17T15:13:23.000Z\" id=\"129528\" provider=\"OpenBetSports\" addr=\"10.99.30.13\"> <betTypeRef id=\"SGL\"/> <stake amount=\"1.00\" freebet=\"0.00\" stakePerLine=\"1.00\"> <currencyRef id=\"GBP\"/> </stake> <payout potential=\"7.00\" winnings=\"0.00\" refunds=\"0.00\" bonus=\"0.0\"/> <lines number=\"1\" win=\"0\" lose=\"0\" void=\"0\"/> <cashoutValue amount=\"0.67\"/> <partialCashout available=\"Y\"/> <leg result=\"-\" documentId=\"1\"> <ns2:sportsLeg> <ns2:price num=\"6\" den=\"1\" legType=\"-\"> <ns2:priceTypeRef id=\"LP\"/> </ns2:price> <ns2:placeTerms/> <ns2:outcomeCombiRef/> <ns2:winPlaceRef id=\"WIN\"/> <ns2:legPart> <ns2:outcomeRef id=\"136410343\"/> </ns2:legPart> </ns2:sportsLeg> </leg> </bet> </cashoutBetResponse>",
                ""));
        operations.add(operation("2016-02-29 12:23", "readBet", "Jimmi1", "O/7523244/0007271", 1100, "Success",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBet adminMode=\"N\" xmlns=\"http://schema.products.sportsbook.openbet.com/bet\"> <customerRef id=\"108993\"/> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <channelRef id=\"M\"/> <cashoutValue amount=\"0.67\"> <currencyRef id=\"GBP\"/> </cashoutValue> </cashoutBet>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBetResponse xmlns=\"http://schema.products.sportsbook.openbet.com/bet\" xmlns:ns2=\"http://schema.products.sportsbook.openbet.com/betcommon\"> <betError code=\"BET_ERROR\" subErrorCode=\"CASHOUT_PENDING\"> <errorDesc>CASHOUT_PENDING</errorDesc> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <cashoutDelay>7</cashoutDelay> <cashoutBetDelayId>18357</cashoutBetDelayId> </betError> <bet receipt=\"O/0108993/0003202\" isSettled=\"N\" isFunded=\"Y\" isConfirmed=\"Y\" timeStamp=\"2016-02-17T15:13:23.000Z\" id=\"129528\" provider=\"OpenBetSports\" addr=\"10.99.30.13\"> <betTypeRef id=\"SGL\"/> <stake amount=\"1.00\" freebet=\"0.00\" stakePerLine=\"1.00\"> <currencyRef id=\"GBP\"/> </stake> <payout potential=\"7.00\" winnings=\"0.00\" refunds=\"0.00\" bonus=\"0.0\"/> <lines number=\"1\" win=\"0\" lose=\"0\" void=\"0\"/> <cashoutValue amount=\"0.67\"/> <partialCashout available=\"Y\"/> <leg result=\"-\" documentId=\"1\"> <ns2:sportsLeg> <ns2:price num=\"6\" den=\"1\" legType=\"-\"> <ns2:priceTypeRef id=\"LP\"/> </ns2:price> <ns2:placeTerms/> <ns2:outcomeCombiRef/> <ns2:winPlaceRef id=\"WIN\"/> <ns2:legPart> <ns2:outcomeRef id=\"136410343\"/> </ns2:legPart> </ns2:sportsLeg> </leg> </bet> </cashoutBetResponse>",
                ""));
        operations.add(operation("2016-02-29 12:24", "cashoutBet", "Jimmi1", "O/7523244/0007271", 1600, "Success",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBet adminMode=\"N\" xmlns=\"http://schema.products.sportsbook.openbet.com/bet\"> <customerRef id=\"108993\"/> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <channelRef id=\"M\"/> <cashoutValue amount=\"0.67\"> <currencyRef id=\"GBP\"/> </cashoutValue> </cashoutBet>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBetResponse xmlns=\"http://schema.products.sportsbook.openbet.com/bet\" xmlns:ns2=\"http://schema.products.sportsbook.openbet.com/betcommon\"> <betError code=\"BET_ERROR\" subErrorCode=\"CASHOUT_PENDING\"> <errorDesc>CASHOUT_PENDING</errorDesc> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <cashoutDelay>7</cashoutDelay> <cashoutBetDelayId>18357</cashoutBetDelayId> </betError> <bet receipt=\"O/0108993/0003202\" isSettled=\"N\" isFunded=\"Y\" isConfirmed=\"Y\" timeStamp=\"2016-02-17T15:13:23.000Z\" id=\"129528\" provider=\"OpenBetSports\" addr=\"10.99.30.13\"> <betTypeRef id=\"SGL\"/> <stake amount=\"1.00\" freebet=\"0.00\" stakePerLine=\"1.00\"> <currencyRef id=\"GBP\"/> </stake> <payout potential=\"7.00\" winnings=\"0.00\" refunds=\"0.00\" bonus=\"0.0\"/> <lines number=\"1\" win=\"0\" lose=\"0\" void=\"0\"/> <cashoutValue amount=\"0.67\"/> <partialCashout available=\"Y\"/> <leg result=\"-\" documentId=\"1\"> <ns2:sportsLeg> <ns2:price num=\"6\" den=\"1\" legType=\"-\"> <ns2:priceTypeRef id=\"LP\"/> </ns2:price> <ns2:placeTerms/> <ns2:outcomeCombiRef/> <ns2:winPlaceRef id=\"WIN\"/> <ns2:legPart> <ns2:outcomeRef id=\"136410343\"/> </ns2:legPart> </ns2:sportsLeg> </leg> </bet> </cashoutBetResponse>",
                ""));
        operations.add(operation("2016-02-29 12:57", "cashoutBet", "Jimmi1", "O/7523244/0007271", 1800, "Success",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBet adminMode=\"N\" xmlns=\"http://schema.products.sportsbook.openbet.com/bet\"> <customerRef id=\"108993\"/> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <channelRef id=\"M\"/> <cashoutValue amount=\"0.67\"> <currencyRef id=\"GBP\"/> </cashoutValue> </cashoutBet>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <cashoutBetResponse xmlns=\"http://schema.products.sportsbook.openbet.com/bet\" xmlns:ns2=\"http://schema.products.sportsbook.openbet.com/betcommon\"> <betError code=\"BET_ERROR\" subErrorCode=\"CASHOUT_PENDING\"> <errorDesc>CASHOUT_PENDING</errorDesc> <betRef id=\"129528\" provider=\"OpenBetSports\"/> <cashoutDelay>7</cashoutDelay> <cashoutBetDelayId>18357</cashoutBetDelayId> </betError> <bet receipt=\"O/0108993/0003202\" isSettled=\"N\" isFunded=\"Y\" isConfirmed=\"Y\" timeStamp=\"2016-02-17T15:13:23.000Z\" id=\"129528\" provider=\"OpenBetSports\" addr=\"10.99.30.13\"> <betTypeRef id=\"SGL\"/> <stake amount=\"1.00\" freebet=\"0.00\" stakePerLine=\"1.00\"> <currencyRef id=\"GBP\"/> </stake> <payout potential=\"7.00\" winnings=\"0.00\" refunds=\"0.00\" bonus=\"0.0\"/> <lines number=\"1\" win=\"0\" lose=\"0\" void=\"0\"/> <cashoutValue amount=\"0.67\"/> <partialCashout available=\"Y\"/> <leg result=\"-\" documentId=\"1\"> <ns2:sportsLeg> <ns2:price num=\"6\" den=\"1\" legType=\"-\"> <ns2:priceTypeRef id=\"LP\"/> </ns2:price> <ns2:placeTerms/> <ns2:outcomeCombiRef/> <ns2:winPlaceRef id=\"WIN\"/> <ns2:legPart> <ns2:outcomeRef id=\"136410343\"/> </ns2:legPart> </ns2:sportsLeg> </leg> </bet> </cashoutBetResponse>",
                ""));
        types.add("cashoutBet");
        types.add("readBet");
    }
    /**
     * To get operations that fit the criteria
     *
     * @param criteria selection criteria
     * @return the list of DTO objects
     */
    @Override
    public List<MonitorOperation> selectOperationsByCriteria(MonitorCriteria criteria) {
        return operations;
    }

    /**
     * To get the list monitored OpenBet operations
     *
     * @return the names list
     */
    @Override
    public List<String> supportedOperationTypes() {
        return types;
    }

    /**
     * To get the quantity of operations that fit the criteria
     *
     * @param criteria selection criteria
     * @return the quantity of operations
     */
    @Override
    public long countOperationByCriteria(MonitorCriteria criteria) {
        return (long)(Math.random() * 100);
    }
    // private methods
    private MonitorOperation operation(String time, String type, String customer, String bet, long duration, String state, String input, String output, String error){
        MonitorOperation operation = new MonitorOperation();
        operation.setTime(time);
        operation.setType(type);
        operation.setCustomer(customer);
        operation.setBet(bet);
        operation.setDuration(duration);
        operation.setState(state);
        operation.setInputXML(input);
        operation.setOutputXML(output);
        operation.setStackTrace(error);
        return operation;
    }
}
