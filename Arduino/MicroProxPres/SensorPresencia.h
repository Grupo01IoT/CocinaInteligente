#ifndef SENSORDEPRESENCIA_YA_INCLUIDO

#define SENSORDEPRESENCIA_YA_INCLUIDO

// CLASE SENSOR PRESENCIA    

    

// SENSOR:

// ESP32 || ESP8266

// Vcc -> 5V || -> Vin

// Lectura -> (*this).pin = GPIO0 || -> D3

// GND -> GND

// --------------------

// LED:

// CORTO -> GND

// LARGO -> GPIO14 || -> D5



class SensorPresencia {

  

  private:

  

    //pin: pin analógico

    int pin;

    bool motion;

    int ledPin;

    bool estadoAnterior;

    bool toggle = 0;

    //int tensiondigital -> funcion() -> double tensionanalogica 

    /*

      double calcular_tension_analogica(int vdig) {

      return (4.096*vdig)/32767;

    }

    */

    

  public:

    //CONSTRUCTOR (_pin: pin de la señal)

    SensorPresencia(const int _pin, const int _pin_led){

      (*this).pin= _pin;

      (*this).ledPin = _pin_led;

    }

    void begin(){

      pinMode((*this).pin, INPUT);

      pinMode((*this).ledPin, OUTPUT);

    }

    //Presencia (Devuelve bool)

      bool hayPresencia(){

        motion = digitalRead((*this).pin);

        if(motion == HIGH){

          Serial.println("Movement detected");

          digitalWrite(ledPin, HIGH);

          return true;

        }else{

          Serial.println("No movement detected");

          digitalWrite(ledPin, LOW);

          return false;

          

        }

        return motion;

    }



    bool togglePresencia(){

      if((*this).hayPresencia() == estadoAnterior){

        return 0;

      }else{

        estadoAnterior = (*this).hayPresencia();

        return 1;

      }

    }

  

  

}; // class SensorTemperatura

#endif