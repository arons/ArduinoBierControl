package ch.arons.bier;

/**
 * PID implementation.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * @author Renzo Dani arons7@gmail.com
 */
public class PID {

	private boolean enabled = true;
	
    private double P;			
    private double I;			
    private double D;			

    private double integralThreshold;		
    
    private double targetpoint;
    
    
    
    private double drive;
    private double error;
    private double previousError;
    private double integratedError;
    
    
    public synchronized void setK(double Kp, double Ki, double Kd) {
        this.P = Kp;
        this.I = Ki;
        this.D = Kd;
    }
    
   
    public synchronized void setTargetpoint(double targetpoint) {
    	if(this.targetpoint != targetpoint) reset();
		this.targetpoint = targetpoint;
		
	}
    
    public void setIntegralThreshold(double integralThreshold) {
		this.integralThreshold = integralThreshold;
	}
    
    
    
    public synchronized double getDrive() {
	   return drive;
    }
    
    public synchronized double getError() {
		return error;
	}
    
    
    
    
    

    
    
    public synchronized void enable() {
        enabled = true;
    }

    public synchronized void disable() {
        enabled = false;
    }
    
    public synchronized void reset() {
        previousError = 0;
        integratedError = 0;
    }
    
    
    /**
     * compute PID computation
     * @param input
     * @return drive
     */
    public synchronized double computePID(double input) {
        // If enabled then proceed into controller calculations
        if (enabled) {

            error = targetpoint - input;
            
            if (integralThreshold <= 0.0 || Math.abs(error) < integralThreshold){ // prevent integral 'windup' 
            	integratedError +=  error; // accumulate the error integral           	 
            } else { 
            	integratedError=0; // zero it if out of bounds 
           	} 


            // PID computation
            drive = P * error + 
            		I * integratedError + 
            		D * (error-previousError);

            previousError = error;

        }else{
        	error = 0;
        	previousError = 0;
        	drive = 0;
        }
        
        return drive;
    }
}
