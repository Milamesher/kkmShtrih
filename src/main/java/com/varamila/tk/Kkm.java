package com.varamila.tk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class Kkm {

    private List<Position> pos;

    public List<Position> getPos() {
        return pos;
    }

    public void setPos(List<Position> pos) {
        this.pos = pos;
    }


    private BigDecimal totalPaymentSum; //итого

    public BigDecimal getTotalPaymentSum() {
        return totalPaymentSum;
    }

    public void setTotalPaymentSum(BigDecimal totalPaymentSum) {
        this.totalPaymentSum = totalPaymentSum;
    }


    private BigDecimal totalTaxSum;  //итого ндс

    public BigDecimal getTotalTaxSum() {
        return totalTaxSum;
    }

    public void setTotalTaxSum(BigDecimal totalTaxSum) {
        this.totalTaxSum = totalTaxSum;
    }

    private BigDecimal totalRefundSum;  //итого вернуть

    public BigDecimal getTotalRefundSum() {
        return totalRefundSum;
    }

    public void setTotalRefundSum(BigDecimal totalRefundSum) {
        this.totalRefundSum = totalRefundSum;
    }

    private String function; //имя ф-ии

    public String getFunction() {
        return function;
    }

    public void setFunction(String functionName) {
        this.function = function;
    }

    private boolean isTerminalPayment;  //нал безнал

    public boolean getIsTerminalPayment() { return isTerminalPayment; }

    public void setIsTerminalPayment(boolean isTerminalPayment) {this.isTerminalPayment=isTerminalPayment;}

    ///
    private String FIO; //ФИО кассира

    public String getFIO() {return FIO;}

    public void setFIO(String FIO) {this.FIO = FIO;}

    public Kkm () {
        pos = new ArrayList<>();
    }


    ///
    private String gotId;  //id счёта, чтобы не повторялось

    public String getGotId() {
        return gotId;
    }

    public void setGotId(String gotId) {
        this.gotId = gotId;
    }

    private String customerPhone; //номер телефона/email

    public String getCustomerPhone() { return customerPhone; }

    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    private boolean isElectronic;  //отправлять эл. чек?

    public boolean getIsElectronic() { return isElectronic; }

    public void setIsElectronic(boolean isElectronic) {this.isElectronic=isElectronic;}
}