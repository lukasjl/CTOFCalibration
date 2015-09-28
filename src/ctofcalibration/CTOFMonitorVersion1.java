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

import org.jlab.evio.clas12.EvioDataEvent;
import org.root.histogram.H1D;
import org.root.histogram.H2D;
import org.root.pad.EmbeddedCanvas;

/**
 *
 * @author lukas
 */
public class CTOFMonitorVersion1 extends DetectorMonitoring  {
        
    private TreeMap<Integer,H1D>  TDCL = new TreeMap<Integer,H1D>();
    private TreeMap<Integer,H1D>  TDCR = new TreeMap<Integer,H1D>();
    private TreeMap<Integer,H1D>  ADCL = new TreeMap<Integer,H1D>();
    private TreeMap<Integer,H1D>  ADCR = new TreeMap<Integer,H1D>();
    private TreeMap<Integer,H1D>  MeanADC = new TreeMap<Integer,H1D>();
    private TreeMap<Integer,H1D>  LRatio = new TreeMap<Integer,H1D>();
    private TreeMap<Integer,H2D>  ADCLvsADCR = new TreeMap<Integer,H2D>();
    private TreeMap<Integer,H1D>  Veff = new TreeMap<Integer,H1D>();
        
    DataTransportVersion1 transport = new DataTransportVersion1();
    
    public CTOFMonitorVersion1(){
        super("CTOF","lukas","1.0");
    }
    
    @Override
    public void processEvent(EvioDataEvent event) {
        List<CTOFPaddleVersion1>  ctof = null;
        if(event.hasBank("CTOF::dgtz")==true){
            ctof = transport.getCTOFPaddlesGEMC(event);
            
        } else {
            ctof = transport.getCTOFPaddlesRAW(event);
        }
        
        if(ctof!=null){
            //System.out.println(" EVENT # ???? ");
            for(CTOFPaddleVersion1 paddle : ctof ){
                //System.out.println(paddle);
                
                H1D histTDCL = this.TDCL.get(paddle.PADDLEID);
                if(histTDCL!=null){
                    histTDCL.fill(paddle.TDCL);
                    //System.out.println("filling");
                }
                
                H1D histTDCR = this.TDCR.get(paddle.PADDLEID);
                if(histTDCR!=null){
                    histTDCR.fill(paddle.TDCR);
                    //System.out.println("filling");
                }
                
                H1D histADCL = this.ADCL.get(paddle.PADDLEID);
                if(histADCL!=null){
                    histADCL.fill(paddle.ADCL);
                    //System.out.println("filling");
                }
                
                H1D histADCR = this.ADCR.get(paddle.PADDLEID);
                if(histADCR!=null){
                    histADCR.fill(paddle.ADCR);
                    //System.out.println("filling");
                }
                
                H1D histMeanADC = this.MeanADC.get(paddle.PADDLEID);
                if(histMeanADC!=null){
                  histMeanADC.fill(paddle.MeanADC);
                    //System.out.println("filling");
                }
                
                H1D histLRatio = this.LRatio.get(paddle.PADDLEID);
                if(histLRatio!=null){
                  histLRatio.fill(paddle.LRatio);
                    //System.out.println("filling");
                }
                
                /*             
                H1D VeffHist = this.Veff.get(paddle.PADDLEID);
                if(histLRatio!=null){
                  VeffHist.fill(paddle.???);
                    //System.out.println("filling");
                }
                */
                H2D histADCLvsADCR = this.ADCLvsADCR.get(paddle.PADDLEID);
                if(histADCLvsADCR!=null){
                  histADCLvsADCR.fill(paddle.ADCL,paddle.ADCR);
                }
                                
            }
        }
    }

    
    public H1D getTDCLHisto(int paddle){
        return this.TDCL.get(paddle);
    }
    
    public H1D getTDCRHisto(int paddle){
        return this.TDCR.get(paddle);
    }
    
    public H1D getADCLHisto(int paddle){
        return this.ADCL.get(paddle);
    }
    
    public H1D getADCRHisto(int paddle){
        return this.ADCR.get(paddle);
    }
    
    public H1D getMeanADCHisto(int paddle){
        return this.MeanADC.get(paddle);
    }
    
     public H1D getLRatioHisto(int paddle){
        return this.LRatio.get(paddle);
    }
    /* 
     public H1D getVeffHisto(int paddle){
        return this.Veff.get(paddle);
    } 
    */ 
    
    public H2D getADCLvsADCRHisto(int paddle){
        return this.ADCLvsADCR.get(paddle);
    }
     
    @Override
    public void init() {
        
        for(int loop = 1; loop <= 48; loop++){
            H1D histTDCL = new H1D("PADDLE",120,50.,70.);
            this.TDCL.put(loop, histTDCL);
            H1D histTDCR = new H1D("PADDLE",120,50.,70.);
            this.TDCR.put(loop, histTDCR);
            
            H1D histADCL = new H1D("PADDLE",120,0.,10000);
            this.ADCL.put(loop, histADCL);
            H1D histADCR = new H1D("PADDLE",120,0.,10000);
            this.ADCR.put(loop, histADCR);
            
            H1D histMeanADC = new H1D("PADDLE",120,0.,10000);
            this.MeanADC.put(loop, histMeanADC);
            
            H1D histLRatio = new H1D("PADDLE",120,-2.,2);
            this.LRatio.put(loop, histLRatio);
            /*
            H1D VeffHist = new H1D("PADDLE",120,-2.,2);
            this.Veff.put(loop,VeffHist);
            */
            H2D histADCLvsADCR = new H2D("PADDLE",120,1000.,10000, 120,1000.,10000);
            this.ADCLvsADCR.put(loop, histADCLvsADCR);
            
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

    
    
    
}

