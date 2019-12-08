#include "SensorPresencia.h"

#include "SensorDeDistancia.h"

#include "SensorDeGas.h"

#include "analogWrite.h"

#include "WiFi.h" 

#include "AsyncUDP.h"



#define PIN_PRESENCIA 5

#define PIN_PRESENCIA_PILOTO 25



#define PIN_DISTANCIA_TRG 26

#define PIN_DISTANCIA_ECHO 36

#define PIN_DISTANCIA_PILOTO 16


#define PIN_GAS 39

#define K 0.000887573964497



#define BUTTON_PIN_BITMASK 2  // IO 2 activas

//RTC_DATA_ATTR definicion de variable en memoria del RTC 
RTC_DATA_ATTR int contador = 0; 
RTC_DATA_ATTR int contador_t = 0; 
RTC_DATA_ATTR int contador1 = 0; 
RTC_DATA_ATTR int contador2 = 0;

//factor de conversion de microsegundos a segundos 
#define uS_TO_S_FACTOR 1000000 
//tiempo que el ESP32 estara dormido (en segundos) 
#define TIME_TO_SLEEP 5

void print_wakeup_reason() {
  esp_sleep_wakeup_cause_t wakeup_reason;
  wakeup_reason = esp_sleep_get_wakeup_cause();
  Serial.println(""); 
  Serial.println(""); 
  Serial.println("EXT1 Test");
  switch (wakeup_reason) { 
    case 1  : Serial.println("Wakeup caused by external signal using RTC_IO"); break; 
    case 2  : {
      Serial.print("Wakeup caused by external signal using RTC_CNTL ");
      uint64_t a =(uint64_t)esp_sleep_get_ext1_wakeup_status();
      if (a==0x2000000) contador1++;
      else if (a==4) contador++;
      else if (a==0x800000000) contador2++;
      Serial.println();
      break;
    } 
    case 3  : Serial.println("Wakeup caused by timer"); contador_t++; break;
    case 4  : Serial.println("Wakeup caused by touchpad"); break; 
    case 5  : Serial.println("Wakeup caused by ULP program"); break; 
    default : Serial.println("Wakeup was not caused by deep sleep"); break;
  }
}



SensorDeDistancia sensor_distancia(PIN_DISTANCIA_TRG, PIN_DISTANCIA_ECHO, PIN_DISTANCIA_PILOTO);

SensorPresencia sensor_presencia(PIN_PRESENCIA, PIN_PRESENCIA_PILOTO);

SensorDeGas sensor_gas(PIN_GAS, K);



const char * ssid = "TEAM_01"; 

const char * password = "123456789";

/*

const char * ssid = "MiFibra-0495"; 

const char * password = "MMhtE79c";*/



char texto[20];

char texto1[20];

boolean rec=0;

bool c = 0;

AsyncUDP udp;



void setup() {

  // put your setup code here, to run once:

  // put your setup code here, to run once:

  Serial.begin(9600);

  sensor_presencia.begin();

  sensor_distancia.begin();
    
  sensor_gas.begin();

  pinMode(2, INPUT_PULLUP);

  print_wakeup_reason();

  

  //ESTA SERÁ LA CLASE DEL PILOTO ->

  //pinMode(PIN_PILOTO, OUTPUT);



  WiFi.mode(WIFI_STA); 

  WiFi.begin(ssid, password); 

  if (WiFi.waitForConnectResult() != WL_CONNECTED) { 

    Serial.println("WiFi Failed"); 

    while(1) { 

      delay(1000); 

      } 

  }   

  if(udp.listen(1234)) { 

    Serial.print("UDP Listening on IP: "); 

    Serial.println(WiFi.localIP()); 

    udp.onPacket([](AsyncUDPPacket packet) {

      int i=20; 

      while (i--) {

        *(texto+i)=*(packet.data()+i);

      } 

      rec=1;        //indica mensaje recibido

    });

  }


  //AQUÍ EMPIEZA EL LOOP
  char texto[20];
  char texto1[20];
  char texto2[20];
  // put your main code here, to run repeatedly:
  if(sensor_presencia.hayPresencia()==1){sprintf (texto1, "P" ); udp.broadcastTo(texto1,1234);}
  else{sprintf (texto1, "N" ); udp.broadcastTo(texto1,1234);};
  // put your main code here, to run repeatedly:
  int distancia = sensor_distancia.distancia();
  Serial.println(distancia);
  c=0;
  if(distancia<10){
    //sensor_distancia.toggleLuz();
    c = 1;
  }
  if(c){
    sprintf (texto, "L");
    udp.broadcastTo(texto,1234);
  }
  if(sensor_gas.hayGas() == true){
    Serial.println("Alerta: fuga de gas.");
    sprintf (texto2, "G");
    udp.broadcastTo(texto2,1234);
  }
  delay(500);
  //programacion del temporizador del RTC para que despierte 
  //al ESP32 al cabo del tiempo definido 
  esp_sleep_enable_timer_wakeup(TIME_TO_SLEEP * uS_TO_S_FACTOR); 
  //programación para usar varias entradas 
  esp_sleep_enable_ext1_wakeup(BUTTON_PIN_BITMASK, ESP_EXT1_WAKEUP_ANY_HIGH);
  esp_deep_sleep_start(); //duerme al ESP32 (modo SLEEP)
}

void loop() {
}
