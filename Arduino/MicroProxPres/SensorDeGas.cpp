#include "SensorDeGas.h"

#include "analogWrite.h"

#import <Arduino.h>



SensorDeGas::SensorDeGas(const int gPin, const float gK){
    
    pin = gPin;
    
    k = gK;
    
}



void SensorDeGas::begin(){
    
    pinMode(pin, INPUT_PULLUP);
    
}

bool SensorDeGas::hayGas(){

  float a = analogRead(pin) * k;
    Serial.println(a);
    if(a >= 3){
        
        return true;
    
    } else {
        
        return false;
        
    }
    
}
