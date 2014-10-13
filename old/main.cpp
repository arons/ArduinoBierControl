#include <WProgram.h>
#include <LiquidCrystal.h>
#include <math.h>
#include <OneWire.h>
#include <PID_v1.h>
#include "main.h"

extern "C" void __cxa_pure_virtual() {
	while (1);
}


// initialize the library with the numbers of the interface pins
LiquidCrystal lcd(7, 6, 5, 4, 3, 2);



//Define Variables we'll be connecting to
double Setpoint, Input, Output;

//Specify the links and initial tuning parameters
PID myPID(&Input, &Output, &Setpoint,200,500,100, DIRECT);
//PID myPID(&Input, &Output, &Setpoint,200,0,0, DIRECT);

unsigned long windowStartTime;




int main(void) {
	init();

	setup();

	for (;;)
		loop();

	return 0;
}





void setup() {


	// set up the LCD's number of columns and rows:
	lcd.begin(16, 2);
	pinMode(relePin, OUTPUT);
	Serial.begin(115200);


	windowStartTime = millis();
	//initialize the variables we're linked to
	Setpoint = targetTemp;
	//tell the PID to range between 0 and the full window size
	myPID.SetOutputLimits(0, WindowSize);
	//turn the PID on
	myPID.SetMode(AUTOMATIC);


	temp_read_start();
}


unsigned long lastDisplayMS;
String releStatus = "";
void lcdDisplay(){

	lcd.clear();

	lcd.setCursor(0, 0);
	lcd.print("target:");
	lcd.print(targetTemp);
	lcd.print(" T:");
	lcd.print(Input);

	lcd.setCursor(0, 1);
	lcd.print("out:");
    lcd.print(Output);
	lcd.print(releStatus);
}


void loop() {


	Input = get_temp();
	myPID.Compute();


	/************************************************
    * turn the output pin on/off based on pid output
    ************************************************/
	unsigned long now = millis();
	if(now - windowStartTime > WindowSize){
	    //time to shift the Relay Window
		windowStartTime += WindowSize;
	}



	if(Output > now - windowStartTime){
		digitalWrite(relePin, LOW);
		releStatus = " ON" ;
	}
	else{
		digitalWrite(relePin,HIGH);
		releStatus = " OFF" ;
	}



	if(now - lastDisplayMS > lcdRefreshMS){
		lastDisplayMS = now;
		lcdDisplay();
	}

	if(time_pause > 0)
		delay(time_pause)	;


/*

	double delta;
	double time;

	lcd.clear();

	lcd.setCursor(0, 0);
	lcd.print("target:");
	lcd.print(targetTemp);
	lcd.print(" T:");
	lcd.print(get_temp());


	lcd.setCursor(0, 1);

	delta = targetTemp - get_temp();


	if(delta > 5.0){
		digitalWrite(relePin, LOW); //on
		lcd.print(" ON ");
		delay(200);
	}else{
		time = delta * time_mult;

		if (delta > target_delta) {


			lcd.print("rMs:");
			lcd.print((int)(time));

			//start
			digitalWrite(relePin, LOW);
			delay(time);
			//stop
			digitalWrite(relePin, HIGH);
		}

		lcd.print(" stop:");
		lcd.print( time / (time + time_pause) );
		delay(time_pause);
	}
*/


}


