package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Button; 
import com.google.gwt.user.client.ui.FlexTable; 
import com.google.gwt.user.client.ui.HorizontalPanel; 
import com.google.gwt.user.client.ui.Label; 
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox; 
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Random;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;

import java.util.Date;
import java.util.ArrayList;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {
	private static final int REFRESH_INTERVAL = 5000; // ms
	
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> stocks = new ArrayList<String>();
	private StockPriceServiceAsync stockPriceSvc = GWT.create(StockPriceService.class);
	private Label errorMsgLabel = new Label();
	private LoginInfo loginInfo = null;
    private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label("Please sign in to your Google Account to access the StockWatcher application.");
	private Anchor signInLink = new Anchor("Sign In");
	private Anchor signOutLink = new Anchor("Sign Out");
	private final StockServiceAsync stockService = GWT.create(StockService.class);
	
	/*
	 * Entry point method
	 */
	public void onModuleLoad() {
		// Check login status using login service.
	    LoginServiceAsync loginService = GWT.create(LoginService.class);
	    loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
	      public void onFailure(Throwable error) {
	      }

	      public void onSuccess(LoginInfo result) {
	        loginInfo = result;
	        if(loginInfo.isLoggedIn()) {
	          loadStockWatcher();
	        } else {
	          loadLogin();
	        }
	      }
	    });
	}
	
	private void loadLogin() {
	    // Assemble login panel.
	    signInLink.setHref(loginInfo.getLoginUrl());
	    loginPanel.add(loginLabel);
	    loginPanel.add(signInLink);
	    RootPanel.get("stockList").add(loginPanel);
	}

	private void loadStockWatcher() {
		// Set up sign out hyperlink.
	    signOutLink.setHref(loginInfo.getLogoutUrl());
		
		// Create table for stock data
		stocksFlexTable.setText(0, 0, "Symbol");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");
		
		// Add styles to elements in the stock list table.
		stocksFlexTable.setCellPadding(6);
	    stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
	    stocksFlexTable.addStyleName("watchList");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
	    
	    loadStocks();
		
		// Assemble Add Stock panel
	    addPanel.add(newSymbolTextBox);
	    addPanel.add(addStockButton);
	    addPanel.addStyleName("addPanel");

	    // Assemble Main panel	    
	    errorMsgLabel.setStyleName("errorMessage");
	    errorMsgLabel.setVisible(false);

	    mainPanel.add(signOutLink);
	    mainPanel.add(errorMsgLabel);
	    mainPanel.add(stocksFlexTable);
	    mainPanel.add(addPanel);
	    mainPanel.add(lastUpdatedLabel);
	    
		// Associate the Main panel with the HTML host page
	    RootPanel.get("stockList").add(mainPanel);
	    
		// Move cursor focus to the input box
	    newSymbolTextBox.setFocus(true);
	    
	    // Setup timer to refresh list automatically.
	    Timer refreshTimer = new Timer() {
	      @Override
	      public void run() {
	        refreshWatchList();
	      }
	    };
	    refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
	    
  	    // Listen for mouse events on the Add button.
	    addStockButton.addClickHandler(new ClickHandler() {
	      public void onClick(ClickEvent event) {
	        addStock();
	      }
	    });
	    
	    // Listen for keyboard events in the input box.
	    newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
	      public void onKeyDown(KeyDownEvent event) {
	        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
	          addStock();
	        }
	      }
	    });
	}
	
	/**
	   * Add stock to FlexTable. Executed when the user clicks the addStockButton or
	   * presses enter in the newSymbolTextBox.
	   */
	  private void addStock() {
		  final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
		  newSymbolTextBox.setFocus(true);

		  // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
		  if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
		    Window.alert("'" + symbol + "' is not a valid symbol.");
		    newSymbolTextBox.selectAll();
		    return;
		  }

		  newSymbolTextBox.setText("");

		  // Don't add the stock if it's already in the table.
		  if (stocks.contains(symbol))
			return;
		  
		  addStock(symbol);
	  }
	  
	  private void addStock(final String symbol) {
		    stockService.addStock(symbol, new AsyncCallback<Void>() {
		      public void onFailure(Throwable error) {
		      }
		      public void onSuccess(Void ignore) {
		        displayStock(symbol);
		      }
		    });
		  }
	  
	  private void loadStocks() {  stockService.getStocks(new AsyncCallback<String[]>() {  public void onFailure(Throwable error) {  }  public void onSuccess(String[] symbols) {  displayStocks(symbols);  }  });  }
	  private void displayStocks(String[] symbols) {  for (String symbol : symbols) {  displayStock(symbol);  }  } 

	  private void displayStock(final String symbol) {
 	      
		  // Add the stock to the table.
		  int row = stocksFlexTable.getRowCount();
		  stocks.add(symbol);
		  stocksFlexTable.setText(row, 0, symbol);
		  stocksFlexTable.setWidget(row, 2, new Label());
		  stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
		  stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
		  stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");
		  
		  // Add a button to remove this stock from the table.
		  Button removeStockButton = new Button("x");
		  removeStockButton.addStyleDependentName("remove");
		  removeStockButton.addClickHandler(new ClickHandler() {
		    public void onClick(ClickEvent event) {
		    	removeStock(symbol);
		    }
		  });
		  stocksFlexTable.setWidget(row, 3, removeStockButton);
		  
		  // Get the stock price.
		  refreshWatchList();
	  }
	  
	  private void removeStock(final String symbol) {
		    stockService.removeStock(symbol, new AsyncCallback<Void>() {
		      public void onFailure(Throwable error) {
		      }
		      public void onSuccess(Void ignore) {
		        undisplayStock(symbol);
		      }
		    });
		  }
	  
	  private void undisplayStock(String symbol) {
		    int removedIndex = stocks.indexOf(symbol);
		    stocks.remove(removedIndex);
		    stocksFlexTable.removeRow(removedIndex+1);
		  }
	  
	  /**
	   * Generate random stock prices.
	   */
	  private void refreshWatchList() {
		// Initialize the service proxy.
	    if (stockPriceSvc == null) {
	      stockPriceSvc = GWT.create(StockPriceService.class);
	    }

	    // Set up the callback object.
	    AsyncCallback<StockPrice[]> callback = new AsyncCallback<StockPrice[]>() {
	      public void onFailure(Throwable caught) {
	    	// If the stock code is in the list of delisted codes, display an error message.
	        String details = caught.getMessage();
	        if (caught instanceof DelistedException) {
	          details = "Company '" + ((DelistedException)caught).getSymbol() + "' was delisted";
	        }

	        errorMsgLabel.setText("Error: " + details);
	        errorMsgLabel.setVisible(true);
	      }

	      public void onSuccess(StockPrice[] result) {
	        updateTable(result);
	      }
	    };

	    // Make the call to the stock price service.
	    stockPriceSvc.getPrices(stocks.toArray(new String[0]), callback);
	  }
	  
	  /**
	   * Update the Price and Change fields all the rows in the stock table.
	   *
	   * @param prices Stock data for all rows.
	   */
	  @SuppressWarnings("deprecation")
	  private void updateTable(StockPrice[] prices) {
	    for (int i = 0; i < prices.length; i++) {
	      updateTable(prices[i]);
	    }
	    
	    // Display timestamp showing last refresh.
	    lastUpdatedLabel.setText("Last update : "  + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
	    
	    // Clear any errors.
	    errorMsgLabel.setVisible(false);
	  }
	  
	  /**
	   * Update a single row in the stock table.
	   *
	   * @param price Stock data for a single row.
	   */
	  private void updateTable(StockPrice price) {
	    // Make sure the stock is still in the stock table.
	    if (!stocks.contains(price.getSymbol())) {
	      return;
	    }

	    int row = stocks.indexOf(price.getSymbol()) + 1;

	    // Format the data in the Price and Change fields.
	    String priceText = NumberFormat.getFormat("#,##0.00").format(
	        price.getPrice());
	    NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
	    String changeText = changeFormat.format(price.getChange());
	    String changePercentText = changeFormat.format(price.getChangePercent());

	    // Populate the Price and Change fields with new data.
	    stocksFlexTable.setText(row, 1, priceText);
	    Label changeWidget = (Label)stocksFlexTable.getWidget(row, 2);
	    changeWidget.setText(changeText + " (" + changePercentText + "%)");
	    
	    // Change the color of text in the Change field based on its value.
	    String changeStyleName = "noChange";
	    if (price.getChangePercent() < -0.1f) {
	      changeStyleName = "negativeChange";
	    }
	    else if (price.getChangePercent() > 0.1f) {
	      changeStyleName = "positiveChange";
	    }

	    changeWidget.setStyleName(changeStyleName);
	  }
}