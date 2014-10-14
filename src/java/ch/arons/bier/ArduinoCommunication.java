package ch.arons.bier;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.xml.internal.security.Init;

public class ArduinoCommunication {
	
	private static final Logger log = LoggerFactory.getLogger(ArduinoCommunication.class);
	
	private static final Object mutex = new Object();
	private static SerialPort serialPort;
	
	public static void init(){
		
		synchronized (mutex) {
			if(serialPort != null){
				return;
			}
			
			

			try {
				CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(ArduinoSerialConfig.portName);
				if (portIdentifier.isCurrentlyOwned()) {
					log.error("Error: Port is currently in use");
					return;
				} 
				
				CommPort commPort = portIdentifier.open(ArduinoCommunication.class.getName(), 2000);
				if (commPort instanceof SerialPort) {
					serialPort = (SerialPort) commPort;
					serialPort.enableReceiveTimeout(2000);
					
					serialPort.setSerialPortParams(
							ArduinoSerialConfig.baud,
							SerialPort.DATABITS_8, 
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					serialPort.enableReceiveThreshold(1);
					serialPort.disableReceiveTimeout();
					
				}else{
					commPort.close();
				}
			} catch (UnsupportedCommOperationException | PortInUseException |NoSuchPortException e) {
				log.error(e.getMessage(),e);
				if(serialPort != null)serialPort.close();
				serialPort = null;
				throw new RuntimeException(e);
			} 
			
		}
	}
	
	public static void close(){
		synchronized (mutex) {
			if(serialPort != null) serialPort.close();
			serialPort = null;
		}
	}
	
	public static Double readtemp() {
		init();
		OutputStream out = null;
		BufferedReader in = null;
		try{			
			out = serialPort.getOutputStream();
			out.write(ArduinoSerialConfig.command_temp.getBytes());
			
			 in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
		    String line = in.readLine();
		    
			return Double.parseDouble(line.replace("temp:", ""));
		} catch (RuntimeException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}finally{
			if(out != null)
				try { out.close(); } catch (IOException e) { }
			if(in != null)
				try { in.close(); } catch (IOException e) { }
		}
		return null;
	}
	
	
	
	public static void sendCommand(String command) {
		init();
		OutputStream out = null;
		try{			
			out = serialPort.getOutputStream();
			out.write(command.getBytes());
		} catch (RuntimeException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}finally{
			if(out != null)
				try { out.close(); } catch (IOException e) { }
		}
	}
}
