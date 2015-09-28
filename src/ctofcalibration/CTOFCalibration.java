/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctofcalibration;



import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioFactory;
import org.jlab.evio.clas12.EvioSource;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;

import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.clasrec.utils.ServiceConfiguration;

import org.jlab.evio.clas12.EvioDataSync;
import org.root.group.TDirectory;
import org.root.histogram.GraphErrors;
import org.root.histogram.H1D;
import org.root.pad.*;
import org.root.group.*;
import org.root.histogram.*;
import org.root.base.IDataSet;
import org.root.data.DataSetXY;
import org.root.func.*;

import org.root.func.F1D;
import org.root.group.TBrowser;
import org.root.histogram.H1D;
import org.root.histogram.H2D;
import org.root.pad.TCanvas;

/**
 *
 * @author gavalian, modified by lukas
 */

public class CTOFCalibration extends DetectorMonitoring {
    public CTOFCalibration() {
		super("CTOF Calibration", "0.1", "Lukas");
	}
    
     public static void main(String[] args) {
        // TODO code application logic here
                 
        String fileGEMC = "/home/lukas/CLAS12/CTOF/Software/ouput_gemc_dvcs_20000.evio";
        String fileRAW  = "/home/lukas/CLAS12/CTOF/Software/evio_ctof1/ctof1_006390.evio";
        //String fileRAW  = "/home/lukas/CLAS12/CTOF/Software/evio_ctof1/ctof1_006390.evio";
        //String fileRAW  = "/home/lukas/CLAS12/CTOF/Software/Rctof/ctof_center.evio";
        
        EvioFactory.loadDictionary("/home/lukas/CLAS12/CTOF/Software/coatjava/etc/bankdefs/clas12");
        EvioSource reader = new EvioSource();
        
        CTOFMonitor  monitor = new CTOFMonitor();
        monitor.init();
        reader.open(fileRAW);
        while(reader.hasEvent()){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            monitor.processEvent(event);
        }
        
        // Test that I can read in the evio file output with the directory contents	
        //TBrowser browser = new TBrowser(monitor.getDir());
		
	TCanvas c1,c2,c3;
	c1 = new TCanvas("Geometric Mean","Geometric Mean",800,800,3,3);
	c2 = new TCanvas("Effective Velocity","Effective Velocity",800,800,3,3);
	c3 = new TCanvas("Log Ratio","Log Ratio",800,800,3,3); 
	// Try out attenuation plot
	//c4 = new TCanvas("Attenuation","Attenuation",1200,800,2,3);
		
        for(int loop = 1; loop <= 9; loop++){
            H1D mLogRatioHist = monitor.mLogRatioHist(loop);
            if(mLogRatioHist!=null){
                // Geometric mean
		// do this later after get fitting to work
		monitor.fitGain();
                double nEntries=monitor.getEntries(monitor.getGeometricMeanHist());
			//System.out.println("nEntries "+nEntries);
			
			int nRebin=(int) (50000/nEntries);           
			if (nRebin>5) {
			nRebin=5;               
			}
                
                c3.cd(loop-1);
                c3.draw(mLogRatioHist);
            }
        }
			
			H1D mGMHistRebin = monitor.getGeometricMeanHist();
			//if(nRebin>0) {
				//mGMHistRebin = rebin(monitor.getGeometricMeanHist(), nRebin);
			//}
			
			// Work out parameter values based on the maximum entries
			// Doing this for now until I know the best replacement for the TSpectrum code
			
			int maxBin = monitor.getMaximumBin(monitor.getGeometricMeanHist());
			double maxCounts = monitor.getGeometricMeanHist().getBinContent(maxBin);
			double maxPos = monitor.getGeometricMeanHist().getAxis().getBinCenter(maxBin);
			
			// the range for the fit is to be 0.5 to 1.6 * position of max
			// OR
			// if position of max is < 2000 then it's to be (0.25 * position of max) to (position of max + 1200)
			
			final double ALT_GM_FIT_LOW_FRACTION = 0.25;
			final double ALT_GM_FIT_HIGH_WIDTH = 1200;
			
			double lowFit, highFit;
			//if (maxPos < GM_FIT_ALT_CUT_OFF) {
			//	lowFit = maxPos * ALT_GM_FIT_LOW_FRACTION;
			//	highFit = maxPos + ALT_GM_FIT_HIGH_WIDTH;
			//}
			//else {
				lowFit = maxPos * 0.5;
				highFit = maxPos * 1.6;
			//}
						
			F1D func = new F1D("landau+exp",lowFit, highFit);
			//F1D func = new F1D("landau",lowFit, highFit);
			//F1D func = new F1D("landau+p1",100.0,2800.0); // Gagik's values
			
			// first draft of parameter setting
			// final method of determining parameters to be confirmed
			
			func.setParameter(0, maxCounts);
			func.setParameter(1, maxPos);
			func.setParameter(2, 100.0);
			func.setParLimits(2, 0.0,400.0);
			func.setParameter(3, 20.0);
			func.setParameter(4, 0.0);
			
			// these are Gagik's values for the exp part
			// doesn't seem to recognise exp, so have changed it to p1 with free params
			//func.setParameter(3, 20.0);
			//func.setParameter(4, 0.0);
			
			monitor.getGeometricMeanHist().fit(func);
			
			//mGeometricMeanPeak = func.getParameters()[1];
			//System.out.println("GM peak position after fitting "+func.getParameters()[1]);
			
			
			//mGeometricMeanHist.setLineColor(4);
			func.show();
			
			//c1.setFontSize(14);
			//c1.cd(0);
			//c1.draw(mGeometricMeanHist);
			//c1.draw(func, "same");
			
			// Calculate and output the calibration values
			
			// Veff
			monitor.fitPlateau();
			monitor.calculateVeffEdges();
			
			F1D veffEdgesFunc = new F1D("p1",monitor.getVeffLeftEdge(), monitor.getVeffRightEdge());
			veffEdgesFunc.setParameter(1, 0.0);
			veffEdgesFunc.setParameter(0, 
					(monitor.getVeffHist().getBinContent(monitor.getVeffHist().getMaximumBin()))/2);
			
			// Log ratio
			monitor.calculateLogRatio();
			
			System.out.println("Paddle "
                                        + "Veff left edge = "+monitor.getVeffLeftEdge()
					+"; Veff right edge = "+monitor.getVeffRightEdge()
					+"; Veff width = "+(monitor.getVeffRightEdge()-monitor.getVeffLeftEdge())
					+"; Geometric Mean peak = "+monitor.getGeometricMeanPeak()
					+"; Log Ratio peak = "+monitor.getLogRatioPeak()
					+"; Log Ratio error = "+monitor.getLogRatioError());			
			
			// Draw the histograms
			monitor.getGeometricMeanHist().setLineColor(2);
			c1.cd(monitor.getPaddleNumber());
			c1.draw(monitor.getGeometricMeanHist(),"*");
			c1.draw(func, "same");
			
			monitor.getVeffHist().setLineColor(2);
			c2.cd((monitor.getPaddleNumber())); 
			c2.draw(monitor.getVeffHist(),"*");
			c2.draw(veffEdgesFunc, "same");
			
			monitor.getLogRatioHist().setLineColor(2);
			c3.cd((monitor.getPaddleNumber()));
			c3.draw(monitor.getLogRatioHist(),"*");
			
			//c4.cd(monitor.getPaddleNumber());
			//c4.draw(monitor.getAttenGraph(),"*");
			

		}
		/*
		// Output a sample histogram as a string
		CTOFMonitor outputPaddle = CTOFMonitor.get("CTOF");
		String outputString = outputPaddle.getGeometricMeanHist().toString();
		try {
			Files.write(Paths.get("geoMeanHist.txt"), outputString.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                */
     
    @Override
    public void processEvent(EvioDataEvent ede) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configure(ServiceConfiguration sc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void analyze() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
		
				 
	}


    

  
	        


