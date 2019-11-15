#include "WiFi.h" 
#include "AsyncUDP.h"

#define PIN_GAS 39
#define THRESHOLD 400

const char * ssid = "TEAM_01"; 
const char * password = "123456789";

char texto[20];
AsyncUDP udp;
boolean rec = 0;

void setup() {
  // put your setup code here, to run once:
  pinMode(PIN_GAS, INPUT);
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
  // put your main code here, to run repeatedly:
  char texto2[20];
  int analogSensor = analogRead(PIN_GAS);
  if (analogSensor > THRESHOLD)
  {
    sprintf (texto2, "G" );
    udp.broadcastTo(texto2,1234);
  }
  else
  {
    
  }
  delay(100);
}
