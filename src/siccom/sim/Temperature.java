package siccom.sim;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class handles the temperature data
 * The different years of the data set are accessed randomly and the 
 * one extreme year (1998) is accessed when the bleaching interval is reached
 *  
 * @author Andreas Kubicek
 * @version 2.0
 */


public class Temperature implements Steppable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 434562251L;

	/**
	 * The main simulation
	 */
	Siccom sim;
	/**
	 * The name of the input file
	 */
	String fileName;
	/**
	 * Scanner to read in the data
	 */
	Scanner scan;
	/**
	 * Daily temperature data for a year
	 */
	ArrayList<Double> yearData;
	/**
	 * Storage for all yearly temperature data sets
	 */
	Hashtable<String, ArrayList<Double>> tempData = new Hashtable<String, ArrayList<Double>>();
	/**
	 * The year name
	 */
	private String yearString;
	/**
	 * Determines if this is the first year
	 */
	boolean firstYear=true;
	/**
	 * The lowest value for year of the data set
	 */
	int minYear;
	/**
	 * The highest value for year of the data set
	 */
	int maxYear;
	/**
	 * The days each month of the year has
	 */
	final int[] daysPerMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	/**
	 * The glider which is used to calculate temperature sums
	 */
	ArrayList<Double> glider = new ArrayList<Double>();
	/**
	 * The number of days in a year, already over
	 */
	int oldDays;
	/**
	 * All old days including the ones of the focal month
	 */
	int allDays = 0;
	/**
	 * The temperature sum of the last day of the last measurement
	 */
	double oldTemp=28;
	/**
	 * Temperature threshold from which on over-temperatures are summed up
	 */
	double threshTemp;
	/**
	 * The over temperature
	 */
	double overTemp = 0;
	/**
	 * Temperatures of the consecutive hot days
	 */
	ArrayList<Double> consecList = new ArrayList<Double>();
	/**
	 * The iterator for the glider
	 */
	int tempIterator = 0;
	/**
	 * The actual year the data is taken from
	 */
	private int year;
	/**
	 * The total sum of exceeding temperature 
	 */
	private double totalTemp = 0;
	/**
	 * The total sum of temperatures divided by the days
	 */
	public double meanTemp;
	/**
	 * A yearly data set 
	 */
	private ArrayList<Double> tempList;
	/**
	 * Stores the temperature values for the calculation
	 */
	private ArrayList<Double> movingWindow = new ArrayList<Double>();
	/**
	 * Determines how many days are taken for the calculation
	 */
	private int movWindowValues = 20;
	/**
	 * The maximum over temperature
	 */
	private double maxOverTemp;

	
	public Temperature(String fileName, Siccom sim)
	{
		this.fileName = fileName;
		File file = new File(Config.getLocalFileName(fileName));
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.sim = sim;
		
		for (int i=0; i<movWindowValues; i++)
				movingWindow.add(i, 28.5);
		
		year = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
	}
	
	/**
	 * Read the temperature file
	 */
	void readTempFile()
	{
		while (scan.hasNextLine())
		{
			String line = scan.nextLine();
			Scanner lineScan = new Scanner(line);
			if (!(line.startsWith("#")))  
			{
				yearData = new ArrayList<Double>();
				yearString = lineScan.next()+"";
				if (firstYear) 
				{
					minYear = Integer.parseInt(yearString);
					firstYear = false;
				}
				tempData.put(yearString, yearData);

				for (int i=0; i<365; i++)
				{
					double t = Double.parseDouble(lineScan.next());
					yearData.add(t);
				}					
				
			}
		}
		maxYear = Integer.parseInt(yearString);
	}
	
	
	/**
	 * Choose a yearly data set
	 */
	public ArrayList<Double> randomize()
	{
		year = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
		return tempData.get(year+"");
	}
	/**
	 * Choose the extreme temperature data set
	 */
	public ArrayList<Double> elNino()
	{
		return tempData.get("1998");
	}
	
	
	/**
	 * adjust the glider for the next calculation
	 * @param days
	 * @param yD
	 */
	public void adjustGlider(int days, ArrayList<Double> yD)
	{
		oldDays = allDays;
		allDays += days;
		
		for (int i=oldDays; i<allDays; i++) 
		{
			glider.add(yD.get(i));
		}
		
	}

	
	/**
	 * Go through the monthly data day by day and keep the ({@link #movWindowValues}-1)  previous days
	 */
	public void glide()
	{
		for (int i=0; i<glider.size(); i++ )
		{
			movingWindow.add(glider.get(i));
				threshTemp = sim.tempSumThreshold;
				for (double t : movingWindow)
				{
					if (t > threshTemp) overTemp += (t - threshTemp);
					if (overTemp > maxOverTemp) maxOverTemp = overTemp;
				}
				sim.overTempPerDay = maxOverTemp / movWindowValues;
				overTemp = 0;
				maxOverTemp = 0;

			movingWindow.remove(0);
		}
	}
	

	
	@Override
	/**
	 * The step routine for the Temperature object
	 * @param state
	 */
	public void step(SimState state) 
	{

		int yearly = (int)(sim.schedule.getSteps()%12);
		int bleacho = (int)(sim.schedule.getSteps()%sim.bleachInterval);

		if ( yearly == 0 ) 
		{ 
			oldDays = 0;
			allDays = 0;
			// at the beginning of each year we choose a year data file
			// --> here we choose between all files but the one with elNino data
			// just if 'bleacho' is true the elNino year is chosen
			if (!(sim.schedule.getSteps() == 0) && bleacho == (sim.firstBleach*12)) 
			{
				tempList = tempData.get("1998");
				System.out.println("1998 The Bleacho");
			}
			else
			{			
				int tempDataYear = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);

				// to make sure that 1998 is not chosen outside the bleaching interval
				while (tempDataYear == 1998) tempDataYear = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);

				tempList = tempData.get(tempDataYear+""); 
				System.out.println(tempDataYear + "");

			}
					
		}
		
		int daysOfMonth = daysPerMonth[yearly];

		adjustGlider(daysOfMonth, tempList);
		glide();

		// Calculate the mean temperature for the month
		for(double t : glider) totalTemp += t;
		meanTemp = totalTemp/(daysOfMonth);
		totalTemp = 0;
		allDays = oldDays + daysOfMonth;
		reschedule(1.0);
	}
	
	/**
	 * Reschedule for the next time step (month)
	 * @param dTime
	 */
	public final void reschedule(double dTime) 
	{
		if (dTime <= 0.0) {
			step(sim);
		}
		sim.schedule.scheduleOnceIn(dTime, this);
	}
}
	
	
