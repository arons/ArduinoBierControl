#include <Arduino.h>
#include "main.h"

void setup() {
	Serial.begin(9600);
	
	pinMode(relePin, OUTPUT);
	digitalWrite(relePin, HIGH);
	
	temp_read_start();
	
	Serial.println("start");
}

void serialCommand() {
	int incomingByte = 0; // for incoming serial data
	// send data only when you receive data:
	if (Serial.available() > 0) {
		// read the incoming byte:
		incomingByte = Serial.read();

		if (incomingByte == 't') {
			Serial.print("temp:");
			Serial.println(get_temp());
		}
		
		if (incomingByte == 'R') {
			//Serial.print("rele ON");
			digitalWrite(relePin, LOW);
		}
		
		if (incomingByte == 'r') {
			//Serial.print("rele OFF");
			digitalWrite(relePin, HIGH);
		}
	}
}
	
void loop() {
	serialCommand();
		
}

