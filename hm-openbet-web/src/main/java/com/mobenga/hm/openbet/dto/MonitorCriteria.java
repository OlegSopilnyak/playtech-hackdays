package com.mobenga.hm.openbet.dto;

import java.util.Objects;

/**
 * Transport class for search criteria
 */
public class MonitorCriteria {
    private String operationType;
    private String fromDate;
    private String toDate;
    private String customer;
    private String bet;

    public boolean isEmpty() {
        return
                Objects.isNull(operationType)
                        && Objects.isNull(fromDate)
                        && Objects.isNull(toDate)
                        && Objects.isNull(customer)
                        && Objects.isNull(bet)
                ;
    }
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitorCriteria that = (MonitorCriteria) o;
        return Objects.equals(getOperationType(), that.getOperationType()) &&
                Objects.equals(getFromDate(), that.getFromDate()) &&
                Objects.equals(getToDate(), that.getToDate()) &&
                Objects.equals(getCustomer(), that.getCustomer()) &&
                Objects.equals(getBet(), that.getBet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperationType(), getFromDate(), getToDate(), getCustomer(), getBet());
    }

    @Override
    public String toString() {
        return "MonitorCriteria{" +
                "operationType='" + operationType + '\'' +
                ", fromDate='" + fromDate + '\'' +
                ", toDate='" + toDate + '\'' +
                ", customer='" + customer + '\'' +
                ", bet='" + bet + '\'' +
                '}';
    }
}
