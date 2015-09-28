/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctofcalibration;

import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.DetectorRawData;
import org.jlab.clas.detector.DetectorType;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.decode.EvioEventDecoder;
import org.jlab.io.decode.AbsDetectorTranslationTable;
import org.root.histogram.H1D;

/**
 *
 * @author gavalian
 */
public class DataTransport {
    String tableString = "/home/lukas/CLAS12/CTOF/Software/coatjava/lib/packages/calibration/ctof/CTOF.table";
    AbsDetectorTranslationTable table   = new AbsDetectorTranslationTable();
    EvioEventDecoder            decoder = new EvioEventDecoder();
        
    public DataTransport(){
        table.readFile(tableString);    
        decoder.addTranslationTable(table);
    }
    
    public  List<CTOFPaddle>  getCTOFPaddlesGEMC(EvioDataEvent event){
        List<CTOFPaddle> list = new ArrayList<CTOFPaddle>();
        if(event.hasBank("CTOF::dgtz")==true){
             EvioDataBank bank = (EvioDataBank) event.getBank("CTOF::dgtz");
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                CTOFPaddle p = new CTOFPaddle();                
                p.PADDLEID   = bank.getInt("paddle", row);
                p.ADCL       = bank.getInt("ADCL",   row);
                p.TDCL       = bank.getInt("TDCL",   row);
                p.ADCR       = bank.getInt("ADCR",   row);
                p.TDCR       = bank.getInt("TDCR",   row);                
                list.add(p);
            }
        }
        return list;
    } 
    
    public  List<CTOFPaddle>  getCTOFPaddlesRAW(EvioDataEvent event){
        
        List<DetectorRawData> rawD = decoder.getDataEntries(event);
        
        decoder.decode(rawD);
        
        List<DetectorRawData> raw  = decoder.getDetectorData(rawD, DetectorType.CTOF);
        //System.out.println("********************  EVENT #");
        for(int loop = 0; loop < rawD.size(); loop++){
        //    System.out.println(rawD.get(loop));
        }
        //System.out.println(" RAW SIZE = " + rawD.size() + "  TRANSLATED = " + raw.size());        
        List<CTOFPaddle> list = new ArrayList<CTOFPaddle>();
        
        for(int loop = 0; loop < 48; loop++){
            List<DetectorRawData> selected = new ArrayList<DetectorRawData>();
            for(DetectorRawData data : raw){
                if(data.getDescriptor().getComponent()==loop){
                    selected.add(data);
                }
            }
            //System.out.println(" COUNTER " + loop + "  RECORDS = " + selected.size());
            if(selected.size()==4){
                CTOFPaddle paddle= new CTOFPaddle();
                paddle.PADDLEID = loop;
                paddle.ADCL = selected.get(0).getIntegral(35,70) - selected.get(0).getIntegral(0,30)*35/30;
                paddle.ADCR = selected.get(1).getIntegral(35,70) - selected.get(1).getIntegral(0,30)*35/30;
                paddle.TDCL = (double) (selected.get(2).getTDC()/1000.);
                paddle.TDCR = (double) (selected.get(3).getTDC()/1000.); 
                list.add(paddle);
            }
        }
        return list;
    }
}
