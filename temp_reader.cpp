#include <WProgram.h>
#include <OneWire.h>
#include <MsTimer2.h>
#include "main.h"

#define CICLE_MILLIS 750

OneWire ds(tempSensor_pin); // on pin 8

byte addr[8];
boolean step1=true;
double digital_temperature;

double get_temp() {
	return digital_temperature;
}


void start_conversion(){
	//Serial.println("start_conversion");
	ds.reset();
	ds.select(addr);
	ds.write(0x44, 1); // start conversion, with parasite power on at the end
}


void readTemp(){
	//Serial.println("readTemp");

	byte i;
	byte present = 0;
	byte data[12];
	int HighByte, LowByte, TReading, SignBit;
	int Tc_100;

	present = ds.reset();
	ds.select(addr);
	ds.write(0xBE); // Read Scratchpad

	for (i = 0; i < 9; i++) { // we need 9 bytes
		data[i] = ds.read();
	}

	//convert
	LowByte = data[0];
	HighByte = data[1];
	TReading = (HighByte << 8) + LowByte;
	SignBit = TReading & 0x8000; // test most sig bit
	if (SignBit) // negative
	{
		TReading = (TReading ^ 0xffff) + 1; // 2's comp
	}
	Tc_100 = (6 * TReading) + TReading / 4; // multiply by (100 * 0.0625) or 6.25

	digital_temperature = Tc_100 / 100.0;
}





void interrupt_handler(){
	//Serial.println("interrupt_handler");

	if(step1){
		start_conversion();
	}else{
		readTemp();
	}
	step1 = !step1;
}


void temp_read_start() {

	//Serial.println("temp_read_start");

	//search Address
	ds.reset_search();
	if (!ds.search(addr)) {
		return;
	}
	if (OneWire::crc8(addr, 7) != addr[7]) {
		return;
	}
	//check for family
	if (addr[0] != 0x28) {
		return;
	}

	//Serial.println("start measure");
	MsTimer2::set(CICLE_MILLIS, interrupt_handler); //
	MsTimer2::start();
}








