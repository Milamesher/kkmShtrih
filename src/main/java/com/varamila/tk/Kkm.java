package com.varamila.tk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Информация для ККМ
 */
public class Kkm {
    /** Список позиций в чеке*/
    private List<Position> pos;
    public List<Position> getPos() {
        return pos;
    }
    public void setPos(List<Position> pos) {
        this.pos = pos;
    }

    /** Итого продажа*/
    private BigDecimal totalPaymentSum;
    public BigDecimal getTotalPaymentSum() {
        return totalPaymentSum;
    }
    public void setTotalPaymentSum(BigDecimal totalPaymentSum) {
        this.totalPaymentSum = totalPaymentSum;
    }

    /** Итого НДС*/
    private BigDecimal totalTaxSum;
    public BigDecimal getTotalTaxSum() {
        return totalTaxSum;
    }
    public void setTotalTaxSum(BigDecimal totalTaxSum) {
        this.totalTaxSum = totalTaxSum;
    }

    /** Итого возврат*/
    private BigDecimal totalRefundSum;
    public BigDecimal getTotalRefundSum() {
        return totalRefundSum;
    }
    public void setTotalRefundSum(BigDecimal totalRefundSum) {
        this.totalRefundSum = totalRefundSum;
    }

    /** Имя функции*/
    private String function;
    public String getFunction() {
        return function;
    }
    public void setFunction(String function) {
        this.function = function;
    }

    /** Тип оплаты (наличными/картой)*/
    private boolean isTerminalPayment;
    public boolean getIsTerminalPayment() { return isTerminalPayment; }
    public void setIsTerminalPayment(boolean isTerminalPayment) {this.isTerminalPayment=isTerminalPayment;}

    /** ФИО кассира*/
    private String FIO;
    public String getFIO() {return FIO;}
    public void setFIO(String FIO) {this.FIO = FIO;}

    /** Id счёта, чтобы избежать повторов*/
    private String gotId;
    public String getGotId() {
        return gotId;
    }
    public void setGotId(String gotId) {
        this.gotId = gotId;
    }

    /** Номер телефона/email*/
    private String customerPhone;
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    /** Отправлять эл. чек?*/
    private boolean isElectronic;
    public boolean getIsElectronic() { return isElectronic; }
    public void setIsElectronic(boolean isElectronic) {this.isElectronic=isElectronic;}

    public Kkm () {
        pos = new ArrayList<>();
    }
}