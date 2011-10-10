
#define relePin 13

#define tempSensor_pin 8


#define targetTemp 60.0
#define target_delta 0.5
#define time_mult 200

//main loop pause
#define time_pause 200

//define controlling windo size
#define WindowSize 5000

//lcd refresh time
#define lcdRefreshMS 200



void temp_read_start();
double get_temp();
