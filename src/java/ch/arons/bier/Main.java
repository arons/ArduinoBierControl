package ch.arons.bier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(ArduinoCommunication.class);
	
	private static long pwmWindow;
	private static final PID pid = new PID();
	
	public static void main(String[] args) throws InterruptedException {
		
		pid.setIntegralThreshold(1.0);
		
		while (true) {
			PIDfromProperty();
			
			Double temp = ArduinoCommunication.readtemp();
			if(temp == null) continue;
			
			long drive = (long) pid.computePID(temp.doubleValue());
			
			
			log.info(" temp:{} drive:{}",temp,drive);
			pwm(pwmWindow, drive);
		}
	}
	
	
	private static void pwm(long window, long up) throws InterruptedException{
		if(up < 0) up = 0;
		if(up > window) up = window;
		long rest = window -up;
		
		if(up > 0){
			ArduinoCommunication.sendCommand(ArduinoSerialConfig.rele_ON);
			Thread.sleep(up);
		}
		
		if(rest > 0){
			ArduinoCommunication.sendCommand(ArduinoSerialConfig.rele_OFF);
			Thread.sleep(rest);
		}
		
	}
	
	
    public static void PIDfromProperty(){
    	InputStream is = null;
        try {
	        is = new FileInputStream(new File("pid.txt"));
	        Properties properties = new Properties();
	        properties.load(is);
	        
	        String window = properties.getProperty("window","0");
	        String target = properties.getProperty("target","0");
	        String p = properties.getProperty("p","0");
	        String i = properties.getProperty("i","0");
	        String d = properties.getProperty("d","0");
	        
	        pwmWindow = Long.valueOf(window);
	        pid.setK(Double.parseDouble(p), Double.parseDouble(i), Double.parseDouble(d));
	        pid.setTargetpoint(Double.parseDouble(target));
	        
        } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        throw new RuntimeException(e);
        } catch (IOException e) {
	        e.printStackTrace();
	        throw new RuntimeException(e);
        }finally{
        	if(is != null) try {
	            is.close();
            } catch (Exception e) {}
        } 
    }
}
