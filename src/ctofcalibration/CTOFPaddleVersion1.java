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
public class CTOFPaddleVersion1 {
    
    public CTOFPaddleVersion1(){
    
    }
    
    public int PADDLEID = 0;
    public double ADCL     = 0;
    public double ADCR     = 0;
    public double TDCL     = 0;
    public double TDCR     = 0;
    public double MeanADC = 0;
    public double LRatio = 0;
    public double ADCLvsADCR = 0;
    
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("%4d %6d %6d %6d %6d", this.PADDLEID,
                this.ADCL,this.ADCR,this.TDCL,this.TDCR));
        return str.toString();
    }
    
}
