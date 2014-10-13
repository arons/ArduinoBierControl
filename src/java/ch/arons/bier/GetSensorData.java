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

public class GetSensorData {
	
	private static final Logger log = LoggerFactory.getLogger(GetSensorData.class);
	
	public static String readMessage() {
		
		CommPort commPort = null;
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier .getPortIdentifier(ArduinoSerialConfig.portName);
			
			
			if (portIdentifier.isCurrentlyOwned()) {
				System.out.println("Error: Port is currently in use");
			} else {
				commPort = portIdentifier.open(GetSensorData.class.getName(), 2000);
				

				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort) commPort;
					serialPort.enableReceiveTimeout(2000);
					
					serialPort.setSerialPortParams(
							ArduinoSerialConfig.baud,
							SerialPort.DATABITS_8, 
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					serialPort.enableReceiveThreshold(1);
					serialPort.disableReceiveTimeout();
					
					OutputStream out = serialPort.getOutputStream();
					out.write(ArduinoSerialConfig.command_request.getBytes());
					
					
					
					BufferedReader in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
					
				    String line = in.readLine();
		            return line;
		            
				} else {
					System.out.println("Error: Only serial ports are handled by this example.");
				}
			}
			
		} catch (RuntimeException e) {
			log.error(e.getMessage(),e);
		} catch (NoSuchPortException e) {
			log.error(e.getMessage(),e);
		} catch (PortInUseException e) {
			log.error(e.getMessage(),e);
		} catch (UnsupportedCommOperationException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}finally{
			if(commPort != null){
				commPort.close();
			}
		}
		
		
		return null;
	}
	
	public static void main(String[] args) {
		log.info("check:");
		log.info(GetSensorData.readMessage());
	}
}
