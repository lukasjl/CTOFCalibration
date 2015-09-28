/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctofcalibration;



import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioFactory;
import org.jlab.evio.clas12.EvioSource;


import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.clasrec.utils.ServiceConfiguration;


import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;
import org.root.pad.TCanvas;

/**
 *
 * @author gavalian, modified by lukas
 */

public class CTOFCalibrationVersion1 extends DetectorMonitoring {
    public CTOFCalibrationVersion1() {
		super("CTOF CalibrationVersion1", "0.1", "Lukas");
	}
    
     public static void main(String[] args) {
        // TODO code application logic here
                 
        String fileGEMC = "/home/lukas/CLAS12/CTOF/Software/ouput_gemc_dvcs_20000.evio";
        String fileRAW  = "/home/lukas/CLAS12/CTOF/Software/evio_ctof1/ctof1_006390.evio";
        //String fileRAW  = "/home/lukas/CLAS12/CTOF/Software/evio_ctof1/ctof1_006390.evio";
        //String fileRAW  = "/home/lukas/CLAS12/CTOF/Software/Rctof/ctof_center.evio";
        
        EvioFactory.loadDictionary("/home/lukas/CLAS12/CTOF/Software/coatjava/etc/bankdefs/clas12");
        EvioSource reader = new EvioSource();
        
        CTOFMonitorVersion1  monitor = new CTOFMonitorVersion1();
        monitor.init();
        reader.open(fileRAW);
        while(reader.hasEvent()){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            monitor.processEvent(event);
        }
        
        // Test that I can read in the evio file output with the directory contents	
        //TBrowser browser = new TBrowser(monitor.getDir());
		
	
     
   // Test that I can read in the evio file output with the directory contents	
        //TBrowser browser = new TBrowser(monitor.getDir());
       
        TCanvas c1 = new TCanvas("c1","CTOF_TDCL",800,800,3,3);
        for(int loop = 0; loop <= 9; loop++){
            H1D histTDCL = monitor.getTDCLHisto(loop);
            if(histTDCL!=null){
                c1.cd(loop-1);
                c1.draw(histTDCL);
            }
        }
        TCanvas c2 = new TCanvas("c2","CTOF_TDCR",800,800,3,3);
        for(int loop = 0; loop <= 9; loop++){
            H1D histTDCR = monitor.getTDCRHisto(loop);
            if(histTDCR!=null){
                c2.cd(loop-1);
                c2.draw(histTDCR);
            }
        }
        
        TCanvas c3 = new TCanvas("c3","CTOF_ADCL",800,800,3,3);
        for(int loop = 0; loop <= 9; loop++){
            H1D histADCL = monitor.getADCLHisto(loop);
            if(histADCL!=null){
                c3.cd(loop-1);
                c3.draw(histADCL);
            }
        }
        TCanvas c4 = new TCanvas("c4","CTOF_ADCR",800,800,3,3);
        for(int loop = 0; loop <= 9; loop++){
            H1D histADCR = monitor.getADCRHisto(loop);
            if(histADCR!=null){
                c4.cd(loop-1);
                c4.draw(histADCR);
            }
        }
        
        TCanvas c5 = new TCanvas("c5","CTOF_MeanADC",800,800,3,3);
        for(int loop = 1; loop <= 9; loop++){
            H1D histMeanADC = monitor.getMeanADCHisto(loop);
            if(histMeanADC!=null){
                c5.cd(loop-1);
                c5.draw(histMeanADC);
            }
        }
        
        TCanvas c6 = new TCanvas("c6","CTOF_LRatio",800,800,3,3);
        for(int loop = 1; loop <= 9; loop++){
            H1D histLRatio = monitor.getLRatioHisto(loop);
            if(histLRatio!=null){
                c6.cd(loop-1);
                c6.draw(histLRatio);
            }
        }
        
         TCanvas c7 = new TCanvas("c7","CTOF_ADCLvsADCR",800,800,3,3);
        for(int loop = 1; loop <= 9; loop++){
            H2D histADCLvsADCR = monitor.getADCLvsADCRHisto(loop);
            if(histADCLvsADCR!=null){
                c7.cd(loop-1);
                c7.draw(histADCLvsADCR);
            }
        }
        /*
        TCanvas c8 = new TCanvas("c7","CTOF_Veff",800,800,3,3);
        for(int loop = 1; loop <= 9; loop++){
            H1D VeffHist = monitor.getVeffHisto(loop);
            if(VeffHist!=null){
                c8.cd(loop-1);
                c8.draw(VeffHist);
            }
        }
        */
        
        
        }

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
    


    

  
	        


