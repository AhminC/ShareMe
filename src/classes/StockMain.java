package com.yahoofinance_api.YahooFinanceAPI;
import java.io.File;
import java.io.IOException;
import java.util.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BasicStroke;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class StockMain {
	JFrame window;
	JButton buttonWatchListTitle;
	JButton buttonSearch;
	JButton buttonEstimate;
	JButton buttonTickers[];
	JTextField text;
	ArrayList<String> listSymbol;
	JTextField textBox;
	JLabel labelBox;
	ChartPanel chartPanel;
	boolean isFirst = true;

	public static void main(String [] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new StockMain();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public StockMain() throws IOException {
		initializeGUI();
	}

	private void initializeGUI() throws IOException {
		int length = 800;
		int width = 875;
		window = new JFrame("Postion");
		window.setVisible(true);
		window.setPreferredSize(new Dimension(width,length));
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(null); 
		window.setBackground(Color.WHITE);


		createButtons(width, length, window);
		createTextBox("");
		createHistroyChart();

		window.pack();

		updateWatchListPeriodically();		
	}

	public void createButtons(int width, int length, JFrame window) throws IOException {
		Scanner readWatchList = new Scanner(new File("WatchList"));
		listSymbol = new ArrayList<String>();

		//Putting in User's wish list into list
		while(readWatchList.hasNextLine()) {
			listSymbol.add(readWatchList.nextLine());
		}

		readWatchList.close();

		buttonWatchListTitle = new JButton("W a t c h  L i s t");
		buttonWatchListTitle.setBounds(0, 0, 250, 70);
		window.add(buttonWatchListTitle);

		buttonSearch = new JButton("S E A R C H");
		buttonSearch.setBounds(250, 0, 620, 70);;
		window.add(buttonSearch);

		buttonEstimate = new JButton("E S T I M A T E");
		buttonEstimate.setBounds(250, 700, 620, 70);;
		window.add(buttonEstimate);

		buttonTickers = new JButton[10];
		for(int i = 0; i < 10; i++){
			final String symbol = listSymbol.get(i);
			buttonTickers[i] = new JButton(symbol);
			buttonTickers[i].setBounds(0,(70*i)+70,250,70);
			window.add(buttonTickers[i]);

			buttonTickers[i].addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {

						createTextBox(symbol);
						displayTrueValue(symbol);
						updateChart(symbol);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}  
			}); 
		}
	}

	public void createTextBox(String symbol) throws IOException {

		if(isFirst) {
			labelBox = new JLabel("");
			labelBox.setBounds(255,70,620,100);
			labelBox.setVerticalAlignment(JLabel.TOP);
			labelBox.setHorizontalAlignment(JLabel.LEFT);
			labelBox.setBackground(Color.WHITE);
			labelBox.setText("<html>Ticker: Symbol of company"
					+ "<br/>Price: Price of company"
					+ "<br/>Bid: What buyers will pay"
					+ "<br/>Ask: What sellers will take"
					+ "<br/>Dvidend: Payment made to shareholders"
					+ "<br/>P/E Ratio: Price per earning ratio");

			labelBox.setBackground(Color.white);		
			window.add(labelBox);	
			isFirst = false;

		}
		else {
			labelBox.setText(" ");
			Stock stock = YahooFinance.get(symbol);
			labelBox.setText("<html>Ticker: " + stock.toString() 
			+ "<br/> Currency: " +stock.getCurrency()
			+ "<br/>" +stock.getQuote()
			+ "<br/>" +stock.getDividend()
			+ "<br/>" +stock.getStats()
					);

			labelBox.setVisible(true);
		}
	}

	public String getSymbolAndPrice(String symbol) throws IOException {
		Stock stock = YahooFinance.get(symbol);
		return stock.toString();
	}	

	public boolean isIncreased(String symbol) throws IOException {
		boolean isIncreased = true;

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		from.add(Calendar.DAY_OF_WEEK, -5);

		Stock stock = YahooFinance.get(symbol);
		List<HistoricalQuote> StockNameQuote = stock.getHistory(from, to, Interval.DAILY);

		if (StockNameQuote.size() >= 2) {
			HistoricalQuote lastQuote = StockNameQuote.get(StockNameQuote.size() - 1);
			HistoricalQuote previousQuote = StockNameQuote.get(StockNameQuote.size() - 2);

			if (lastQuote.getAdjClose().doubleValue() < previousQuote.getAdjClose().doubleValue()) {
				isIncreased = false;
			}
		}
		return isIncreased;
	}

	public void displayTrueValue(String symbol) throws IOException {
		int scoreOfStock = 0;		

		Stock stock = YahooFinance.get(symbol);
		String PE = stock.getStats().toString();
		String PEVALUE = PE.substring(PE.indexOf("PE:") + 4, PE.indexOf(", E"));

		boolean condition = true;
		if(PEVALUE.equals("null")){
			condition = false;
			System.out.println("Null value");
		} else {
			System.out.println(PEVALUE);

			float stockPE = Float.parseFloat(PEVALUE);

			if (stockPE > 25) {
				scoreOfStock+=3;
			}else if (stockPE > 17) {
				scoreOfStock +=1;
			}else if (stockPE > 15) {
				scoreOfStock-=1;
			}else {
				scoreOfStock-=3;
			}
			
			
			
			
			
			//ScoreOFStock should be valued through a point systenm
			
			
//			System.out.println(scoreOfStock);
		}
	}
	public void updateWatchList() throws IOException {
		window.setTitle( "Position: " + new Date());

		for(int i = 0; i < 10; i++){
			String symbol = listSymbol.get(i);
			buttonTickers[i].setText(getSymbolAndPrice(symbol));

			if (isIncreased(symbol)) {
				buttonTickers[i].setBackground(Color.GREEN);
			}else {
				buttonTickers[i].setBackground(Color.RED);
			}

			//Need for Mac
			//			buttonTickers[i].setOpaque(true);
			//			buttonTickers[i].setBorderPainted(false);

		}		
	}

	public void updateWatchListPeriodically() throws IOException {
		updateWatchList();
		TimerTask repeatedTask = new TimerTask() {
			public void run() {
				System.out.println("Task performed on " + new Date());
				try {
					updateWatchList();
				} catch (IOException e) {
					e.printStackTrace();

				}
			}
		};

		Timer timer = new Timer("Timer");

		long delay = 10000L;
		long period = 10000L;
		timer.scheduleAtFixedRate(repeatedTask, delay, period);
		//if delay occurs set time *120
		
	}

	public void createHistroyChart() throws IOException {
		XYDataset dataset = createDataset("STNE");
		JFreeChart chart = createChart(dataset);
		chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.white);
		chartPanel.setBounds(255,175,618,500);

		//		labelBox.setBounds(255,70,620,100);

		window.add(chartPanel);
	}

	public XYDataset createDataset(String symbol) throws IOException {
		Stock stock = YahooFinance.get(symbol);
		List<HistoricalQuote> quotes = stock.getHistory(Interval.DAILY);

		XYSeries series = new XYSeries(quotes.get(0).toString());
		for (int i=0; i<quotes.size(); i++) {
			series.add(i, quotes.get(i).getClose());
		}

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);

		return dataset;
	}

	public JFreeChart createChart(XYDataset dataset) {

		//ToDO: Change Graph type to eliminate my dots
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Stock price history",
				"Month",
				"Price",
				dataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);

		XYPlot plot = chart.getXYPlot();

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));

		plot.setRenderer(renderer);
		plot.setBackgroundPaint(Color.white);

		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);

		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.BLACK);

		chart.getLegend().setFrame(BlockBorder.NONE);
		chart.setTitle(new TextTitle("One Year", new Font("Serif", java.awt.Font.BOLD, 18)));

		return chart;
	}

	public void updateChart(String symbol) throws IOException {
		XYDataset dataset = createDataset(symbol);
		JFreeChart chart = createChart(dataset);

		chartPanel.setChart(chart);
		chartPanel.updateUI();
	}
}