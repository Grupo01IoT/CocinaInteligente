#include "SensorPresencia.h"
#include "SensorDeDistancia.h"
#include "analogWrite.h"
#include "WiFi.h" 
#include "AsyncUDP.h"

#define PIN_PRESENCIA 5
#define PIN_PRESENCIA_PILOTO 25

#define PIN_DISTANCIA_TRG 26
#define PIN_DISTANCIA_ECHO 36
#define PIN_DISTANCIA_PILOTO 16

SensorDeDistancia sensor_distancia(PIN_DISTANCIA_TRG, PIN_DISTANCIA_ECHO, PIN_DISTANCIA_PILOTO);
SensorPresencia sensor_presencia(PIN_PRESENCIA, PIN_PRESENCIA_PILOTO);

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

}

void loop() {
  
  char texto[20];
  char texto1[20];
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
  
  delay(500);
  

}
