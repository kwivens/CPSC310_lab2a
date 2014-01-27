package com.google.gwt.sample.stockwatcher.client;

import java.io.Serializable;


public class StockPrice implements Serializable {

	  private String symbol;
	  private double price;
	  private double change;
	  private String userAtest;
	  private String userBtest;
	  private String finaltest;	  

	  public StockPrice() {
	  }

	  public StockPrice(String symbol, double price, double change) {
	    this.symbol = symbol;
	    this.price = price;
	    this.change = change;
	    this.userAtest = "TESTING UPDATE";
	    this.userBtest = "NOW WE DANCE";
	    this.finaltest = "FINAL_FINAL_FINAL_FINAL";
	  }

	  public String getSymbol() {
	    return this.symbol;
	  }

	  public double getPrice() {
	    return this.price;
	  }

	  public double getChange() {
	    return this.change;
	  }

	  public double getChangePercent() {
	    return 100.0 * this.change / this.price;
	  }

	  public void setSymbol(String symbol) {
	    this.symbol = symbol;
	  }

	  public void setPrice(double price) {
	    this.price = price;
	  }

	  public void setChange(double change) {
	    this.change = change;
	  }
}
