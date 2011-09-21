#include <WProgram.h>
#include <LiquidCrystal.h>
#include <math.h>
#include <OneWire.h>
#include "main.h"

extern "C" void __cxa_pure_virtual() {
	while (1);
}


// initialize the library with the numbers of the interface pins
LiquidCrystal lcd(7, 6, 5, 4, 3, 2);

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

	temp_read_start();
}




void loop() {
	double delta;

	lcd.clear();

	lcd.setCursor(0, 0);
	lcd.print("target:");
	lcd.print(targetTemp);
	lcd.print(" T:");
	lcd.print(get_temp());


	lcd.setCursor(0, 1);

	delta = targetTemp - get_temp();
	delta = delta * time_mult;

	if (delta > target_delta) {
		lcd.print(" rMs:");
		lcd.print((int)(delta));

		//start
		digitalWrite(relePin, LOW);
		delay(delta);
		//stop
		digitalWrite(relePin, HIGH);
	}

	lcd.print(" stop:");
	lcd.print( delta / (delta + time_pause) );
	delay(time_pause);


}


