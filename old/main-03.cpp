#include <WProgram.h>
#include <LiquidCrystal.h>
#include <math.h>
#include <OneWire.h>

extern "C" void __cxa_pure_virtual() {
	while (1)
		;
}

int main(void) {
	init();

	setup();

	for (;;)
		loop();

	return 0;
}

// initialize the library with the numbers of the interface pins
LiquidCrystal lcd(7, 6, 5, 4, 3, 2);

OneWire ds(8); // on pin 10

#define ThermistorPIN 0                 // Analog Pin 0
//Steinhart-Hart / B parameter equation
float vcc = 5.00; // only used for display purposes, if used
// set to the measured Vcc.
float serialR = 10000; // serialResistance
//from datasheet
float B = 3950;
float R0 = 10000;
float T0 = 298.15; //Kelvin = 25 Celsius

void Thermistor_B_Print(int RawADC) {
	float Vx;
	float Resistance;
	float Temp;

	Vx = vcc * (RawADC / 1024.0);
	Resistance = ((5 - Vx) / Vx) * serialR;
	Temp = 1 / (1 / T0 + (1 / B) * log(Resistance / R0));
	Temp = Temp - 273.15; // Convert Kelvin to Celsius
	// Uncomment this line for the function to return Fahrenheit instead.
	//temp = (Temp * 9.0)/ 5.0 + 32.0;                  // Convert to Fahrenheit

//  lcd.setCursor(0, 0);
//  lcd.print("RawADC:");
//  lcd.print(RawADC);

//  lcd.print(" Vx:");
//  lcd.print(Vx);

	lcd.setCursor(0, 0);
	lcd.print("ohm:");
	lcd.print(Resistance);

	lcd.print(" C:");
	lcd.print(Temp);
}

void Digital_B_Print(void) {
	byte i;
	byte present = 0;
	byte data[12];
	byte addr[8];

	int HighByte, LowByte, TReading, SignBit, Tc_100, Whole, Fract;


	ds.reset_search();
	if (!ds.search(addr)) {
		Serial.print("No more addresses.\n");

		lcd.setCursor(0, 1);
		lcd.print("No more addresses.");

		return;
	}

	Serial.print("R=");
	for (i = 0; i < 8; i++) {
		Serial.print(addr[i], HEX);
		Serial.print(" ");
	}

	if (OneWire::crc8(addr, 7) != addr[7]) {
		Serial.print("CRC is not valid!\n");
		lcd.setCursor(0, 1);
		lcd.print("CRC is not valid!");
		return;
	}
	/*
	 if ( addr[0] != 0x10) {

	 Serial.print("Device is not a DS18S20 family device.\n");

	 lcd.setCursor(0, 1);
	 lcd.print("Device is not a DS18S20 family device.");

	 ds.reset_search();
	 return;
	 }
	 */
	if (addr[0] != 0x28) {

		Serial.print("Device is not a DS18B20 family device.\n");

		lcd.setCursor(0, 1);
		lcd.print("Device is not a DS18B20 family device.");

		return;
	}

	ds.reset();
	ds.select(addr);
	ds.write(0x44, 1); // start conversion, with parasite power on at the end

	delay(1000); // maybe 750ms is enough, maybe not
	// we might do a ds.depower() here, but the reset will take care of it.

	present = ds.reset();
	ds.select(addr);
	ds.write(0xBE); // Read Scratchpad

	Serial.print("P=");
	Serial.print(present, HEX);
	Serial.print(" ");
	for (i = 0; i < 9; i++) { // we need 9 bytes
		data[i] = ds.read();
		Serial.print(data[i], HEX);
		Serial.print(" ");
	}
	Serial.print(" CRC=");
	Serial.print(OneWire::crc8(data, 8), HEX);
    Serial.println();




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

	Whole = Tc_100 / 100; // separate off the whole and fractional portions
	Fract = Tc_100 % 100;


	lcd.setCursor(0, 1);
	Serial.print("digital:");


	if (SignBit) // If its negative
	{
		Serial.print("-");
		lcd.print("-");
	}
	Serial.print(Whole);
	lcd.print(Whole);

	Serial.print(".");
	lcd.print(".");


	if (Fract < 10) {
		Serial.print("0");
		lcd.print("0");
	}
	Serial.print(Fract);
	lcd.print(Fract);

	Serial.print("\n");
}

void setup() {
	// set up the LCD's number of columns and rows:
	lcd.begin(16, 2);
	Serial.begin(115200);
}

int i = 0;
void loop() {
	Thermistor_B_Print(analogRead(ThermistorPIN));
	Digital_B_Print();
	delay(2000);

	//lcd.print(i);
	//lcd.print(" ");
}

