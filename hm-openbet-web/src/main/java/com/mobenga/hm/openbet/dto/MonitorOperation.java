package com.mobenga.hm.openbet.dto;

import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.OpenBetOperation;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Transport class for monitored operation
 */
public class MonitorOperation implements Serializable{
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private String time;
    private String type;
    private String customer;
    private String bet;
    private long duration;
    private String state;
    private String inputXML;
    private String outputXML;
    private String stackTrace;

    public MonitorOperation() {
    }

    public MonitorOperation(OpenBetOperation operation, MonitoredAction monitoredAction) {
        time = timeFormat.format(monitoredAction.getStart());
        type = operation.getType().name();
        customer = operation.getCustomerId();
        bet = operation.getReceipt();
        duration = monitoredAction.getDuration();
        state = monitoredAction.getState().name();
        inputXML = operation.getInputXML();
        outputXML = operation.getOutputXML();
        stackTrace = operation.getStackTrace();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getBet() {
        return bet;
    }

    public void setBet(String bet) {
        this.bet = bet;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInputXML() {
        return inputXML;
    }

    public void setInputXML(String inputXML) {
        this.inputXML = inputXML;
    }

    public String getOutputXML() {
        return outputXML;
    }

    public void setOutputXML(String outputXML) {
        this.outputXML = outputXML;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public String toString() {
        return "MonitorOperation{" +
                "time='" + time + '\'' +
                ", type='" + type + '\'' +
                ", customer='" + customer + '\'' +
                ", bet='" + bet + '\'' +
                ", duration=" + duration +
                ", state='" + state + '\'' +
                '}';
    }
}
