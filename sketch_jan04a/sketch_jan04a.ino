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
  pinMode(buttonPin, INPUT); //declare pushbutton as input
    // Begin the serial monitor at 9600bps

  
}

void loop()
{
  buttonState = digitalRead(buttonPin);
  if (buttonState == HIGH){
  Serial.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  Serial.println("hello world");
  delay(1000);
  }else{
    buttonState == LOW;
  }
  // and loop forever and ever!
}
