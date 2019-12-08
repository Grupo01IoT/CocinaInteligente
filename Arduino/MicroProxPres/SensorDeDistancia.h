#ifndef SENSORDEDISTANCIA_YA_INCLUIDO

#define SENSORDEDISTANCIA_YA_INCLUIDO



// SENSOR:                         

// ESP32 || ESP8266                           

// Vcc -> 5V || -> Vin

// Trig -> 26 || -> D2

// Echo -> SVP || -> D1

// GND -> GND

// --------------------

// LED:

// CORTO -> GND

// LARGO -> RX || -> D4





class SensorDeDistancia {

  private:

  int EchoPin;

  int TriggerPin;

  int ledPin;

  int freq;

  int ledChannel;

  int resolution;

  int dutyCycle;

  bool encendido;



  public:

  SensorDeDistancia(const int _pin_trg, const int _pin_echo,const int _pin_led);

  void begin();

  int distancia();

  void toggleLuz();

  bool getEncendido();

  void setEncendido(bool toggle);

};



#endif