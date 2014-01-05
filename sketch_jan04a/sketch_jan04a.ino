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
int buttonPin = 9; // choose the input pin (for a pushbutton)
int buttonState = 0;
char incomingBytpe; //incoming data

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
  pinMode(A3, OUTPUT);
  digitalWrite(A3, LOW);
  pinMode(9, INPUT_PULLUP);
  

  
}

void loop()
{
  
  if(digitalRead(9) == LOW)
  {
    Serial.begin(115200); 
    Serial.println("Tagged");
     delay(25);
     while(digitalRead(9) == LOW) ;
  }
   // The Bluetooth Mate defaults to 115200bps
  
 
  
  // and loop forever and ever!
}
