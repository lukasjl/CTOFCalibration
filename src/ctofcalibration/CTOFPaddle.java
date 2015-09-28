/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctofcalibration;

/**
 *
 * @author lukas
 */
public class CTOFPaddle {
    
    public CTOFPaddle(){
    
    }
    
    int PADDLEID = 0;
    int ADCL     = 0;
    int ADCR     = 0;
    double TDCL     = 0;
    double TDCR     = 0;
    //public double Centroid = 0;
    //public double LRatio = 0;
    //double ADCLvsADCR = 0;
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("%4d %6d %6d %6d %6d", this.PADDLEID,
                this.ADCL,this.ADCR,this.TDCL,this.TDCR));
        return str.toString();
    }
}
