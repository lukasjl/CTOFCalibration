/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctofcalibration;

import java.util.List;
import java.util.TreeMap;
import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.clasrec.utils.ServiceConfiguration;

import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.pad.EmbeddedCanvas;
import static java.lang.Math.sqrt;
import org.root.histogram.*;
import org.root.func.*;

/**
 *
 * @author gavalian, modified by lukas
 */
public class CTOFMonitor extends DetectorMonitoring{
    
    //private TreeMap<Integer,H1D>  Centroid = new TreeMap<Integer,H1D>();
    //private TreeMap<Integer,H1D>  mLogRatioHist = new TreeMap<Integer,H1D>();
    //private TreeMap<Integer,H2D>  ADCLvsADCR = new TreeMap<Integer,H2D>();
    
    DataTransport transport = new DataTransport();
    private Object mGeometricMeanHist;
    
    @Override
    public void processEvent(EvioDataEvent event) {
        List<CTOFPaddle>  ctof = null;
        if(event.hasBank("CTOF::dgtz")==true){
            ctof = transport.getCTOFPaddlesGEMC(event);
            
        } else {
            ctof = transport.getCTOFPaddlesRAW(event);
        }
        
        if(ctof!=null){
            System.out.println(" EVENT # ???? ");
            for(CTOFPaddle paddle : ctof){
                System.out.println(paddle);
            }
        }
        
    }
        
        /*
        public void fill(CTOFPaddle paddle){
            System.out.println(paddle.PADDLEID);
            System.out.println(paddle.ADCL + paddle.ADCR);
            System.out.println(paddle.TDCL + paddle.TDCR);
            //System.out.println(paddle.Centroid + paddle.LRatio);
        }
        */
        private int mPaddleNumber;
	
	// Histograms
	public H1D mGeometricMeanHist(int paddle){
        return this.mGeometricMeanHist.get(paddle);
        }
        
	private H1D mVeffHist;
	private H1D mLogRatioHist;
	
	// Calculated calibration values
	private double mVeffLeftEdge;
	private double mVeffRightEdge;
	private double mLogRatioPeak;
	private double mLogRatioError;
	private double mGeometricMeanPeak;
	
	// Try some attenuation plots
	private double[] x = {0.0};
	private double[] y = {0.0};
	private GraphErrors mAttenGraph;
        double LogRatio;
        
        public GraphErrors getAttenGraph() {
		return mAttenGraph;
        }    
        
        // Constants
	final double LOG_RATIO_X_THRESHOLD = 500;
	final double LOG_RATIO_THRESHOLD_FRACTION = 0.2;
	final double PEDESTAL_CUT_OFF = 300;
	final double GM_FIT_LOW_FRACTION = 0.5;
	final double GM_FIT_HIGH_FRACTION = 1.6;
	final double ALT_GM_FIT_LOW_FRACTION = 0.25;
	final double ALT_GM_FIT_HIGH_WIDTH = 1200;
	final double GM_FIT_ALT_CUT_OFF = 2000;
        
         // Getters
	public H1D getGeometricMeanHist() {
		return mGeometricMeanHist;
	}
	
	public H1D getVeffHist() {
		return mVeffHist;
	}	

	public H1D getLogRatioHist() {
		return mLogRatioHist;
	}	
	
	public int getPaddleNumber() {
		return mPaddleNumber;
	}
	
	public double getVeffLeftEdge() {
		return mVeffLeftEdge;
	}
	
	public double getVeffRightEdge() {
		return mVeffRightEdge;
	}
	
	public double getLogRatioPeak() {
		return mLogRatioPeak;
	}
	
	public double getLogRatioError() {
		return mLogRatioError;
	}
	
	public double getGeometricMeanPeak() {
		return mGeometricMeanPeak;
	}
        
        CTOFMonitor() {
                         
                mPaddleNumber = paddle.PADDLEID;
		                
                mGeometricMeanHist = new H1D("Geo Mean", 1000, 0.0, 3000.0);
		
		mGeometricMeanHist.setTitle("Paddle " + mPaddleNumber);
		
		mVeffHist = new H1D("V eff", 1500, -1500.0, 1500.0);
		
		mVeffHist.setTitle("Paddle " + mPaddleNumber);
		
		mLogRatioHist = new H1D("Log Ratio", 500, -3.0, 3.0);
		
		mLogRatioHist.setTitle("Paddle " + mPaddleNumber);		
		
		mAttenGraph = new GraphErrors(x,y);
		//mAttenGraph.add(1.0, 2.0);
		//mAttenGraph.add(2.0, 4.0);
        }
        
        // Calibration calculation methods (same as Louise's methods)
	public void fillHists(CTOFPaddle paddle, double adcRight, double adcLeft, double tdcLeft, double tdcRight){
        
        // fill the geomean histogram with sqrt(adc left * adc right) for the current paddle
    	// fill the veff histogram with (tdc left / tdc right) / 2
        // fill the log ratio histogram with log (adc right / adc left)
		
	// The following code is only in testGeometicMean, so I'm not sure if this should be included
	            
                if (paddle.ADCL>PEDESTAL_CUT_OFF && paddle.ADCR>PEDESTAL_CUT_OFF) {
			
                        adcLeft = paddle.ADCL - PEDESTAL_CUT_OFF;
                        
                        adcRight = paddle.ADCR - PEDESTAL_CUT_OFF;
                      
			double geoMean = Math.sqrt((adcLeft)*(adcRight));
			mGeometricMeanHist.fill(geoMean);
			
			if (geoMean > LOG_RATIO_X_THRESHOLD) {
				mLogRatioHist.fill(Math.log1p(adcRight/adcLeft));			
			}	
			//System.out.println(" EVENT # ");
			// try out attenuation plot
                        
			double xAtten = (paddle.TDCL - paddle.TDCR) / 2;
			
			if ((xAtten>-1500)&&(xAtten<1500)&&(geoMean>45)&&(geoMean<46)) {
				mAttenGraph.add(xAtten, paddle.ADCR);
				mAttenGraph.add(xAtten, paddle.ADCL);
			}
		}
		mVeffHist.fill((paddle.ADCL - paddle.ADCR) / 2);
	}
        
         public int getMaximumBin(H1D h1) { 
		
    	// get the bin with maximum contents
		
		int nBins = h1.getAxis().getNBins();
		int maximumBin = -1;

		for (int i=0; i<=nBins; i++) {
			if (h1.getBinContent(i) > h1.getBinContent(maximumBin)) {
				maximumBin = i;
			}
		}
		return maximumBin;
	}
         
         public double getEntries(H1D h1) {
		
    	// get the total entries in the histogram
		
		int nBins = h1.getAxis().getNBins();
		double totalEntries = -1;

		for (int i=0; i<=nBins; i++) {
			totalEntries = totalEntries+h1.getBinContent(i);
		}
		return totalEntries;
	}	
         
        private H1D rebin(H1D h1, int nBinsCombine) {
		
		int nBinsOrig = h1.getAxis().getNBins();
                System.out.println("min "+ (int) h1.getAxis().min());
                
    H1D h1Rebinned = new H1D("Rebinned", nBinsOrig/nBinsCombine, h1.getAxis().min(), h1.getAxis().max());
		
		int newBin = 0;
		
		for (int origBin=0; origBin<=nBinsOrig;) {
			
			double newBinCounts = 0;
			for (int i=0; i<nBinsCombine; i++) {
				newBinCounts = newBinCounts + h1.getBinContent(origBin);
				origBin++;				
			}
			h1Rebinned.setBinContent(newBin, newBinCounts);
			newBin++;
		}
		return h1Rebinned;
	}
        
        public void fitPlateau() {
		
    	// get the bin with maximum contents
	// get the bins left and right of this bin with contents < 5 sigma less than max
	// (sigma = sqrt(max counts)
	// if < 20 bins, then rebin and repeat

		int lowBin = 0, highBin =0;		
		H1D veffHistSmoothed = mVeffHist;
		int nBinsCombine = 1;
		
		boolean isSmoothed = false;
		while (!isSmoothed) {
			
			int nBins = veffHistSmoothed.getAxis().getNBins();
			int maximumBin = getMaximumBin(veffHistSmoothed);
			double maxCounts = veffHistSmoothed.getBinContent(maximumBin);
			double sigma = Math.sqrt(maxCounts);
			
			for (int i=maximumBin; i>=1; i--) {
				if (veffHistSmoothed.getBinContent(i) < maxCounts - (5*sigma)) {
					lowBin = i;
					break;
				}
			} 
		
			for (int i=maximumBin; i<=nBins; i++) {
				if (veffHistSmoothed.getBinContent(i) < maxCounts - (5*sigma)) {
					highBin = i;
					break;
				}
			} 
			
			if ((highBin - lowBin > 20) || (lowBin <=1 & highBin == nBins)) {
				isSmoothed = true;
			}
			else {
				System.out.println("rebinning "+ nBinsCombine);
				nBinsCombine++;
				veffHistSmoothed = rebin(mVeffHist, nBinsCombine);
			}
		} //while
	}	
        
        public void calculateVeffEdges() {
		
    	// get the bins left and right of the max bin with contents closest to half maximum		

		int maxBin = getMaximumBin(mVeffHist);
		double halfMax = mVeffHist.getBinContent(maxBin) / 2;
		double minDifference = halfMax;
		double binDifference;
		int leftHalfMaxBin = -1;
		
		for (int i=1; i<maxBin; i++) {
			
			binDifference = Math.abs(mVeffHist.getBinContent(i) - halfMax);
			if (binDifference < minDifference) {
				minDifference = binDifference;
				leftHalfMaxBin = i;
			}
		}
		
		minDifference = halfMax;
		int rightHalfMaxBin = -1;
	
		for (int i=maxBin+1; i<= mVeffHist.getAxis().getNBins(); i++) {
			
			binDifference = Math.abs(mVeffHist.getBinContent(i) - halfMax);
			if (binDifference < minDifference) {
				minDifference = binDifference;
				rightHalfMaxBin = i;
			}
		}
		mVeffLeftEdge = mVeffHist.getAxis().getBinCenter(leftHalfMaxBin);
		mVeffRightEdge = mVeffHist.getAxis().getBinCenter(rightHalfMaxBin);
	}	
        
        public void calculateLogRatio() {
		
		// calculate the mean value using portion of the histogram where counts are > 0.2 * max counts
		double sum =0;
		double sumWeight =0;
		double sumSquare =0;
		int maxBin = getMaximumBin(mLogRatioHist);
		double maxCounts = mLogRatioHist.getBinContent(maxBin);
		int nBins = mLogRatioHist.getAxis().getNBins();
		boolean lowThresholdReached = false;
		boolean highThresholdExceeded = false;
		
		
		for (int i=1; i<=nBins; i++) {
			
			// check if we're within the thresholds
			if (!lowThresholdReached) {
				
				if (mLogRatioHist.getBinContent(i) > (LOG_RATIO_THRESHOLD_FRACTION*maxCounts) && i<maxBin) {
					lowThresholdReached = true;
				}				
			}
			
			if (lowThresholdReached && !highThresholdExceeded) {
				
				if (mLogRatioHist.getBinContent(i) < (LOG_RATIO_THRESHOLD_FRACTION*maxCounts) && i>maxBin) {
					highThresholdExceeded = true;
				}				
			}
			
			// include the values in the sum if we're within the thresholds
			if (lowThresholdReached && !highThresholdExceeded) {
				
				double value=mLogRatioHist.getBinContent(i);
				double middle=mLogRatioHist.getAxis().getBinCenter(i);
				
				sum+=value;
				sumWeight+=value*middle;
				sumSquare+=value*middle*middle;
			}			
		}
                
        if (sum>0) {
			mLogRatioPeak=sumWeight/sum;
			mLogRatioError=Math.sqrt((sumSquare/sum)-mLogRatioPeak*mLogRatioPeak);
		}
		else {
			mLogRatioPeak=0.0;
			mLogRatioError=0.0;
		}
	}
        
        public void fitGain() {

		// Try and reproduce fitGain functionality here
		// rebins the histogram depending on the number of entries
		// uses getEntries and rebin methods - don't think these are in the new library
		// then uses the TSpectrum.search method to find at least one peak in the histogram
		// is TSpectrum in a library somewhere?		
		// does a fit of expo(0)+landau(2), uses the peaks found with TSpectrum as parameters
			
		double nEntries=getEntries(mGeometricMeanHist);
		//System.out.println("nEntries "+nEntries);
		
		int nRebin=(int) (50000/nEntries);           
		if (nRebin>5) {
			nRebin=5;               
		}
		
		H1D mGMHistRebin = mGeometricMeanHist;
		if(nRebin>0) {
			mGMHistRebin = rebin(mGeometricMeanHist, nRebin);
		}
		
		// Work out parameter values based on the maximum entries
		// Doing this for now until I know the best replacement for the TSpectrum code
		
		int maxBin = getMaximumBin(mGeometricMeanHist);
		double maxCounts = mGeometricMeanHist.getBinContent(maxBin);
		double maxPos = mGeometricMeanHist.getAxis().getBinCenter(maxBin);
		
		// the range for the fit is to be 0.5 to 1.6 * position of max
		// OR
		// if position of max is < 2000 then it's to be (0.25 * position of max) to (position of max + 1200)

		
		double lowFit, highFit;
			lowFit = maxPos * GM_FIT_LOW_FRACTION;
			highFit = maxPos * GM_FIT_HIGH_FRACTION;
					
		F1D func = new F1D("landau+exp",lowFit, highFit);
		
		// first draft of parameter setting
		// final method of determining parameters to be confirmed
		
		func.setParameter(0, maxCounts);
		func.setParameter(1, maxPos);
		func.setParameter(2, 100.0);
		func.setParLimits(2, 0.0,400.0);
		func.setParameter(3, 20.0);
		func.setParameter(4, 0.0);
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

    H1D mLogRatioHist(int loop) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

/*
        
        if(ctof!=null){
            //System.out.println(" EVENT # ???? ");
            for(CTOFPaddle paddle : ctof){
                //System.out.println(paddle);
                H1D mLogratioHist = this.mLogRatioHist.get(paddle.LogRatio);
                if(mLogRatioHist!=null){
                    hist.fill(mLogratio);
                    //System.out.println("filling");
                }
               
                                
            }
        }
       */   

    /*    
    @Override
    public void init() {
        for(int loop = 1; loop <= 48; loop++){
            H1D histTDCL = new H1D("PADDLE",120,50.,70.);
            this.TDCL.put(loop, histTDCL);
            
        }
    }

    @Override
    public void configure(ServiceConfiguration c) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void analyze() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void drawComponent(int sector, int layer, int component, EmbeddedCanvas canvas){
        
    }
    */




