package siccom.gui;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;

import sim.util.media.chart.TimeSeriesChartGenerator;

/**
 * Once 10 years of simulation time are exceeded the charts are set from 
 * static to dynamic 
 * 
 * @author Andreas Kubicek
 * @version 2.0
 */

public class DynamicCharter extends TimeSeriesChartGenerator
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7899793031369189378L;
	private ValueAxis axis;
	private ValueAxis axis2;

	public void setDynamic()
    {
    	axis = ((XYPlot)(chart.getPlot())).getDomainAxis();
    	axis.setRange(0, 10);
    	axis.setAutoRange(true);
    	axis.setFixedAutoRange(10.0);  // 60 seconds
    	axis2 = ((XYPlot)(chart.getPlot())).getRangeAxis();
    	axis2.setAutoRangeMinimumSize(10);
    }
}
