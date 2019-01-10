package siccom.gui;

import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.jfree.data.xy.XYSeries;
import siccom.Main;
import siccom.Welcome;
import siccom.sim.BranchingGroup;
import siccom.sim.Siccom;
import siccom.sim.Filter;
import siccom.sim.MassiveGroup;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.util.media.chart.TimeSeriesAttributes;

/**
 * The GUI for Siccom.java
 * 
 * @author Andreas Kubicek
 * @version 2.0
 *
 */

public class SiccomUI extends GUIState
{

	long startTime;
	DecimalFormatSymbols usFS = new DecimalFormatSymbols(Locale.US);
	public DecimalFormat numform = new DecimalFormat("00", usFS);
	
	/**
	 * The display for the simulation
	 */
	public Display2D display;
	static int displayWidth = (int)Siccom.gridWidth+10;
	
	/**
	 * The frame for the display
	 */
	public static JFrame displayFrame;
	/**
	 * The simulation that is represented
	 */
	Siccom sim;	
	/**
	 * The mason controller
	 */
	public Controller c;
	/**
	 * The welcome frame
	 */
	Welcome welcome;
	/**
	 * The information for the Population Size time series chart
	 */
	TimeSeriesChartInformation tsPop;
	/**
	 * The information for the Relative Cover time series chart
	 */
	TimeSeriesChartInformation tsCov;
	TimeSeriesChartInformation tsTemp;
	/**
	 * The console which is used for the simulation
	 */
	public static Console cons;
	/**
	 * The width of the console
	 */
	static int consWidth = 400;
	/**
	 * The height of the console
	 */
	static int consHeight = 400;
	/**
	 * The number of massive coral groups
	 */
	public static int maCoGroupNum = 1;
	/**
	 * The number of branching coral groups
	 */
	public static int braCoGroupNum = 1;
	/**
	 * The the variable for group numbers for the parameter initialization
	 */
	private int num;
	/**
	 * This class	
	 */
	public static SiccomUI siccomGUI;
	/**
	 * Portrayal for massive corals; for portrayals see the MASON documentation
	 */
	ContinuousPortrayal2D massiveCoralsPortrayal = new ContinuousPortrayal2D();
	/**
	 * Portrayal for branching corals; for portrayals see the MASON documentation
	 */
	ContinuousPortrayal2D branchingCoralsPortrayal = new ContinuousPortrayal2D();
	/**
	 * Portrayal for macroalgae; for portrayals see the MASON documentation
	 */
	ContinuousPortrayal2D algaePortrayal = new ContinuousPortrayal2D();
	/**
	 * Portrayal for turf algae; for portrayals see the MASON documentation
	 */
	SparseGridPortrayal2D  turfPortrayal = new SparseGridPortrayal2D();
	/**
	 * JFrame for the Population Size Chart
	 */
	private JFrame popFrame;
	/**
	 * JFrame for the Relative Cover Chart
	 */
	private JFrame covFrame;
	private JFrame tempFrame;
	/**
	 * {@link DynamicCharter} for the Population Size Chart
	 */
	private DynamicCharter popChart;
	/**
	 * {@link DynamicCharter} for the Relative Cover Chart
	 */
	private DynamicCharter covChart;
	/**
	 * ArrayList in which the different panels for parameter files are stored 
	 */
	public static ArrayList<FileParameterPanel> panels = new ArrayList<FileParameterPanel>();

	/**
	 * The main class if the program is started directly from CRPSWithUI
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		siccomGUI = new SiccomUI();
		siccomGUI.initParameters();		
		cons = new Console(siccomGUI);
		cons.setSize(consWidth, consHeight);
		cons.setVisible(true);
	}
	
	/**
	 * Constructor for SiccomUI
	 * Setup the GUI synchronized with the CRPS model
	 * Informs the CRPS model that it has to init from the GUI parameter tables
	 */
	public SiccomUI()
	{
		super(new Siccom(System.currentTimeMillis()));
		Siccom.gui = true;
		Siccom.createOutput = false;
	}
	
	/**
	 * Constructor which is invoked from the first constructor 
	 * @param state the state in which the simulation is at the moment
	 */
	public SiccomUI(SimState state)
	{
		super(state);
		sim = (Siccom)state;
	}
	
	/**
	 * Passes the name of the model to the console
	 * @return name of the model
	 */
	public static String getName()
	{
		return "siccom";
	}
	
	
	/**
	 * Passes the simulation state
	 */
	public Object getSimulationInspectedObject()
	{
		return state;
	}

	/**
	 * Initialises the parameter files and sets up the parameter panels with parameter tables
	 */
	public void initParameters()
	{
		readEnvironmentFile();
		readAlgaFile();
		num = maCoGroupNum;
		readParamFiles("maCo");
		num = braCoGroupNum;
		readParamFiles("braCo");
	}
	



	private void readAlgaFile()
	{
		String path = Main.class.getProtectionDomain().getCodeSource()
		.getLocation().getPath();
		File algaFile = new File(path);
		algaFile = new File(algaFile.getParent() + "/inf/algaParam.inf");
		
		FileParameterPanel parm = new FileParameterPanel(this, algaFile , "algaParam.inf");
		panels.add(parm);
	}
	
	
	private void readEnvironmentFile() {
		
		String path = Main.class.getProtectionDomain().getCodeSource()
					.getLocation().getPath();
		File envFile = new File(path);
		envFile = new File(envFile.getParent() + "/inf/environment.inf");
				
		FileParameterPanel parm = new FileParameterPanel(this, envFile , "environment.inf");
		panels.add(parm);
	}

	/**
	 * Reads the parameter files
	 * @param prefix the prefix for either massive or branching corals
	 */
	private void readParamFiles(String prefix) 
	{
		String path = Main.class.getProtectionDomain().getCodeSource()
						.getLocation().getPath();

		File directory = new File(path);
		directory = new File(directory.getParent() + "/inf");

		
		
		File[] files = directory.listFiles(new Filter(prefix, "inf"));
		for (int i=0; i<=num; i++)
		{
			String fileName = prefix + "Param" + i + ".inf";
			for (int index = 0; index < files.length; index++)  
			{  
				if (fileName.equals(files[index].getName()))
				{
					FileParameterPanel parm = new FileParameterPanel(this, files[index], fileName);
					panels.add(parm);
				}
			}	
		}
	}

	/**
	 * Quits the GUI and the simulation and closes all related windows
	 */
	public void quit()
	{
		super.quit();
		if (cons != null) cons.dispose();
		if (popFrame != null) popFrame.dispose();
		if (covFrame != null) covFrame.dispose();
		if (tempFrame != null) tempFrame.dispose();
		if (displayFrame != null) displayFrame.dispose();
		displayFrame = null;	// let gc
		display = null;			// let gc	
		removeModFiles();
		
	}
	
	/**
	 * Finishes the simulation but the console stays.
	 * Parameters can be adjusted and the simulation can be rerun.
	 * 
	 */
	public void finish()
	{
		super.finish();

		long endTime = System.currentTimeMillis();
		long extTime = endTime - startTime;
		double mins = (int)(extTime / 60000);
		double secs = (int)((extTime -(mins*60000)) /1000);
		System.out.println("Execution Time: " + numform.format(mins) + " min " + numform.format(secs) + " sec");
	}
	
	/**
	 * Kick off for the model and setup graphical representation
	 */
	public void start()
	{
		super.start();
		
		startTime = System.currentTimeMillis();
	
		setupPortrayals();
		setupPopChart();
		setupCovChart();
		
		scheduleRepeatingImmediatelyAfter(new Steppable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3516492019851259862L;

			
			public void step(SimState state) 
			{
				double t = state.schedule.time()/12;				// time displayed in years
				
				updateCharts(t);
			}
		});
	}

	/**
	 * Sets up the chart window and table for Population sizes 
	 */
	private void setupPopChart() 
	{
		{
			TimeSeriesAttributes tsa;
			popChart = tsPop.getChart();
			popChart.removeAllSeries();
			popChart.setYAxisLogScaled(true);
			
			XYSeries series[] = new XYSeries[sim.maCoGroups.size()+sim.braCoGroups.size()+1];
			tsPop.setSeries(series);
		
			int i = 0;
			for (MassiveGroup mG : sim.maCoGroups)
			{
				series[i] = new XYSeries(mG.name, true);
				popChart.addSeries(series[i], null);
				tsa = (TimeSeriesAttributes) popChart.getSeriesAttribute(i);
				tsa.setStrokeColor(mG.color);
				i++;
			}
			for (BranchingGroup bG : sim.braCoGroups)
			{
				series[i] = new XYSeries(bG.name, true);
				popChart.addSeries(series[i], null);
				tsa = (TimeSeriesAttributes) popChart.getSeriesAttribute(i);
				tsa.setStrokeColor(bG.color);
				i++;
			}
			series[i] = new XYSeries("Algae", true);
			popChart.addSeries(series[i], null);
			tsa = (TimeSeriesAttributes) popChart.getSeriesAttribute(i);
			tsa.setStrokeColor(Color.green);
		}
	}
	
	/**
	 * Sets up the window and the table for Relative Cover
	 */
	private void setupCovChart() 
	{
		{
			TimeSeriesAttributes tsa;
			covChart = tsCov.getChart();
			covChart.removeAllSeries();

			XYSeries series[] = new XYSeries[sim.maCoGroups.size()+sim.braCoGroups.size()+2];
			tsCov.setSeries(series);

			int i = 0;
			
			for (MassiveGroup mG : sim.maCoGroups)
			{
				series[i] = new XYSeries(mG.name, true);
				covChart.addSeries(series[i], null);
				tsa = (TimeSeriesAttributes) covChart.getSeriesAttribute(i);
				tsa.setStrokeColor(mG.color);
				i++;
			}
			
			for (BranchingGroup bG : sim.braCoGroups)
			{
				series[i] = new XYSeries(bG.name, true);
				covChart.addSeries(series[i], null);
				tsa = (TimeSeriesAttributes) covChart.getSeriesAttribute(i);
				tsa.setStrokeColor(bG.color);
				i++;
			}


			series[i] = new XYSeries("Algae", true);
			covChart.addSeries(series[i], null);
			tsa = (TimeSeriesAttributes) covChart.getSeriesAttribute(i);
			tsa.setStrokeColor(Color.green);
			i++;
			
			
			series[i] = new XYSeries("allCorals", true);
			covChart.addSeries(series[i], null);
			tsa = (TimeSeriesAttributes) covChart.getSeriesAttribute(i);
			tsa.setStrokeColor(Color.black);
			
		}
	}

	
	private void updateCharts(double t) 
	{
		// at this stage we're adding data to our chart. We
		// need an X value and a Y value. Typically the X
		// value is the schedule's time stamp. The Y value
		// is whatever data you're extracting from your
		// simulation. 
		
		if (t >= 10)
		{
			popChart.setDynamic();
			covChart.setDynamic();
//			tempChart.setDynamic();
		}
		// now add the data
		if (t >= Schedule.EPOCH && t < Schedule.AFTER_SIMULATION) 
		{
		
			// Population Size
		 	XYSeries popSeries[] = tsPop.getSeries();
		 	int j = 0;
			for (MassiveGroup mG : sim.maCoGroups)
			{
				int count = mG.getNum();
				popSeries[j].add(t,count, true);
				j++;
			}
			for (BranchingGroup bG : sim.braCoGroups)
			{
				int count = bG.getNum();
				popSeries[j].add(t,count, true);
				j++;
			}
			int algaCount = sim.numAlgae;
			popSeries[j].add(t,algaCount, false);
			tsPop.startTimer(0);

			 // Relative Cover
			
			double allCover=0;
			
			 XYSeries covSeries[] = tsCov.getSeries();
			 j = 0;
			 for (MassiveGroup mG : sim.maCoGroups)
			 {
				 double cover = mG.getCover();
				 allCover += cover; 
				 covSeries[j].add(t, cover, true);
				 j++;
			 }
			 for (BranchingGroup bG : sim.braCoGroups)
			 {
				 double cover = bG.getCover();
				 allCover += cover; 
				 covSeries[j].add(t, cover, true);
				 j++;
			 }

			 double algalCover = sim.algaeCover();
			 covSeries[j].add(t,algalCover, false);
			 tsCov.startTimer(0);
			 j++;
			 
			 covSeries[j].add(t,allCover, false);
			 tsCov.startTimer(0);
			 
		}
	}
	
	
	
	/**
	 * Allows to start from a specified checkpoint
	 */
	public void load(SimState state)
	{
		super.load(state);
		setupPortrayals();
		setupPopChart();				// find out how to get the chart after using a checkpoint file
		setupCovChart();
		
		scheduleRepeatingImmediatelyAfter( new Steppable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3516492019851259863L;

			public void step(SimState state) 
			{
				double t = state.schedule.time();

				updateCharts(t);
			}
		});
	}


	/**
	 * Sets up the portrayals for the different object types
	 */
	public void setupPortrayals()
	{
		/*
		 * tell the portrayals what to
		 * portray and how to portray them
		 */
		sim = (Siccom)state;
		
		massiveCoralsPortrayal.setField( sim.massiveCorals );
		branchingCoralsPortrayal.setField(sim.branchingCorals);
		algaePortrayal.setField( sim.algae );
		turfPortrayal.setField( sim.turf );
		
				
		// reschedule the displayer
		display.reset();
		
		// redraw the display
		display.repaint();
	}

	/**
	 * Initialises the MASON controller
	 * @see the MASON documentation
	 */
	public void init(Controller c)
	{
		super.init(c);

		display = new Display2D(Siccom.gridWidth, Siccom.gridHeight, this, 1);
		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		display.setBackdrop(Color.BLACK);
		
		
		// now attach the field portrayals
		display.attach(turfPortrayal, "Turf");
		display.attach(massiveCoralsPortrayal, "Massive Corals");
		display.attach(branchingCoralsPortrayal, "Branching Corals");
		display.attach(algaePortrayal, "Algae");
		
		initGraphDisplays(c);

		covFrame.setLocation((int)displayWidth+consWidth+200,0);
		covFrame.setSize(1000, 500);
		covFrame.setVisible(true);
		
		
		for (int i=0; i<panels.size(); i++)
		   {
			String fn = panels.get(i).fileName;
			String name = fn.substring(0, fn.length()-4);
		
			((Console)c).getTabPane().addTab(name, 
					new JScrollPane(panels.get(i)));  
		   }
	}
	
	

	/**
	 * Initializes the displays for the graphs
	 * @param c the MASON controller
	 */
	public void initGraphDisplays(Controller c) 
	{
		{
			// Population Size
			tsPop = new TimeSeriesChartInformation(this);
			popFrame = tsPop.create(
					"Populations of Corals and Algae", "Time (years)", "Population Size");
			// perhaps you might move the chart to where you like.
			popFrame.setLocation((int)Siccom.gridWidth+30, consHeight + 5);
			popFrame.setDefaultCloseOperation(1);	// hide on close
			popFrame.pack();
			c.registerFrame(popFrame);

			// RelativeCover
			tsCov = new TimeSeriesChartInformation(this);
			covFrame = tsCov.create(
					"Relative Cover of Corals and Algae", "Time (years)", "Relative Cover (%)");
			// perhaps you might move the chart to where you like.
			covFrame.setLocation((int)Siccom.gridWidth+60, consHeight + 25);
			covFrame.setDefaultCloseOperation(1);	// hide on close
			covFrame.pack();
			c.registerFrame(covFrame);

		}
	}
	
	
	/**
	 * Removes all the files out of the inf-Folder which are setup if the user changed 
	 * data on the parameter files.
	 */
	private void removeModFiles() {
		File directory = new File(Main.class.getProtectionDomain().getCodeSource()
			.getLocation().getPath());  

		directory = new File(directory.getParent() +"/inf");

		File[] files = directory.listFiles(new Filter("", "mod"));  
		  
		if(!(files.length==0))	{ for (File f : files ) f.deleteOnExit();}  

	}
}
