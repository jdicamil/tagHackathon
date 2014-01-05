/*
  Example Bluetooth Serial Passthrough Sketch
 by: Jim Lindblom
 SparkFun Electronics
 date: February 26, 2013
 license: Public domain

 This example sketch converts an RN-42 bluetooth module to
 communicate at 9600 bps (from 115200), and passes any serial
 data between Serial Monitor and bluetooth module.
 */
#include <SoftwareSerial.h>  

int bluetoothTx = 2;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 3;  // RX-I pin of bluetooth mate, Arduino D3

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
    // Begin the serial monitor at 9600bps

  Serial.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  
  while(1){
  Serial.println("hello world");
  int Jonathan = 5;
  Serial.print(Jonathan);
  delay(1000);
  }
}

void loop()
{

  // and loop forever and ever!
}
