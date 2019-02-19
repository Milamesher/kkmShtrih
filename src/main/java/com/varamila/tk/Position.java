package com.varamila.tk;

import java.math.BigDecimal;

/**
 * Позиция в чеке
 */
public class Position {
    /** Код услуги*/
    private String code;
    public String getCode() { return this.code; }
    public void setCode(String code) { this.code = code; }

    /** Наименование услуги*/
    private String name;
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    /** Кол-во услуг в чеке*/
    private int count;
    public int getCount() { return this.count; }
    public void setCount(int count) { this.count = count; }

    /** Цена за одну услугу*/
    private BigDecimal price;
    public BigDecimal getPrice() { return this.price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    /** Цена за все услуги*/
    private BigDecimal sum;
    public BigDecimal getSum() { return this.sum; }
    public void setSum(BigDecimal sum) { this.sum = sum; }

    /** НДС в процентах (10 20 30)*/
    private BigDecimal taxName;
    public BigDecimal getTaxName() { /*return "Ставка НДС " + this.taxName + "%";*/ return  this.taxName; }
    public void setTaxName(BigDecimal taxName) { this.taxName=taxName; }

    /** Сумма НДС в процентах*/
    private String taxSum;
    public String getTaxSum() { return this.taxSum; }
    public void setTaxSum(String taxSum) { this.taxSum = taxSum; }

    public Position(String name,String code, int count,BigDecimal price,BigDecimal sum, BigDecimal taxName,String taxSum) {
        this.name=name; this.code=code; this.count=count; this.price=price; this.sum=sum; this.taxName=taxName; this.taxSum = taxSum;
    }
    public Position () {}
}