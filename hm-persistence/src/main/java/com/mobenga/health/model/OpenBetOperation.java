package com.mobenga.health.model;

/**
 * The Logical Model Bean to keep information about Bet Operation
 */
public abstract class OpenBetOperation {
    public static final String STORAGE_NAME = "openbet-operation";
    /**
     * To get PK for bet operation
     *
     * @return PK
     */
    public abstract String getId();

    /**
     * To get PK of related MonitoredAction of operation
     *
     * @return action PK
     */
    public abstract String getActionId();
    public abstract void setActionId(String actionId);

    /**
     * To get the type of bet-operation
     *
     * @return the type
     */
    public abstract Type getType();
    public abstract void setType(Type type);

    /**
     * To get user-token of opeartion
     *
     * @return user-token
     */
    public abstract String getUserToken();
    public abstract void setUserToken(String userToken);

    /**
     * To get ID of owner bet-operation
     *
     * @return owner's ID
     */
    public abstract String getCustomerId();
    public abstract void setCustomerId(String customerId);

    /**
     * To get the ID of placed bet
     * @return the ID (may be null)
     */
    public abstract String getBetId();
    public abstract void setBetId(String betId);

    /**
     * To get receipt of placed and approved bet
     *
     * @return receipt (may be null)
     */
    public abstract String getReceipt();
    public abstract void setReceipt(String receipt);

    /**
     * To get input parameter as XML document
     *
     * @return xml
     */
    public abstract String getInputXML();
    public abstract void setInputXML(String inputXML);

    /**
     * To get response as XML
     *
     * @return xml (may be null)
     */
    public abstract String getOutputXML();
    public abstract void setOutputXML(String outputXML);

    /**
     * To get java stack-trace in case exception was thrown
     *
     * @return stack-trace (may be null)
     */
    public abstract String getStackTrace();
    public abstract void setStackTrace(String stackTrace);

    /**
     * The types of Bet operations
     */
    public enum Type{
        readBet,
        cashoutBet,
        checkBets,
        placeBets,
        cancelBet,
        acceptOrDeclineOfferBet,
        buildBets,
        buildComplexLegs
    }
}
