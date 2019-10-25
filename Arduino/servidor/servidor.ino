#include "WiFi.h" 
#include "AsyncUDP.h"
#include <M5Stack.h>
#include <string.h> 

#define BLANCO 0XFFFF 
#define NEGRO 0 
#define ROJO 0xF800 
#define VERDE 0x07E0 
#define AZUL 0x001F

#define PINALTAVOZ 18
#define PINEXTRACTOR 17
#define PINLUZ 4
#define PINPILOTOPRESENCIA 15



const char * ssid = "TEAM_01"; 
const char * password = "123456789";
char texto[20];
boolean rec=0;
AsyncUDP udp;

String temperatura = "20";
String luces = "OFF";
String presencia = "ON";
String forUART = "";
void setup() { 
  M5.begin(); 
  M5.Lcd.setTextSize(2); //Tamaño del texto
  Serial.begin(9600); 
  WiFi.mode(WIFI_STA); 
  WiFi.begin(ssid, password); 
  if (WiFi.waitForConnectResult() != WL_CONNECTED) { 
    //Serial.println("WiFi Failed"); 
    while(1) { 
      delay(3000); 
      } 
  }   
  if(udp.listen(1234)) { 
    //Serial.print("UDP Listening on IP: "); 
    //Serial.println(WiFi.localIP()); 
    udp.onPacket([](AsyncUDPPacket packet) {
      int i=20; 
      while (i--) {
        *(texto+i)=*(packet.data()+i);
      } 
      rec=1;        //indica mensaje recibido
    });
  }

   pinMode(PINALTAVOZ, OUTPUT);
   pinMode(PINEXTRACTOR, OUTPUT);
   pinMode(PINLUZ, OUTPUT);
   pinMode(PINPILOTOPRESENCIA, OUTPUT);
   digitalWrite(PINPILOTOPRESENCIA, HIGH);
   digitalWrite(PINLUZ, LOW);
   
   
   //delay(1000);
   //Serial.println("INICIO");
}

void loop() { 
  
  if (rec){ //Send broadcast 
    
    rec=0;              //mensaje procesado
    udp.broadcastTo("Recibido",1234);   //envia confirmacion 
    udp.broadcastTo(texto,1234);            //y dato recibido 
    //Serial.println(texto);
    //SI EL TEXTO ES TL == TOGGLE DE LUZ
    if(strcmp(texto, "L")== 0){
      
      //TOGGLE LED LUCES
      if(digitalRead(PINLUZ) == HIGH){digitalWrite(PINLUZ, LOW); luces = "OFF";}
      else if(digitalRead(PINLUZ) == LOW){digitalWrite(PINLUZ, HIGH); luces = "ON";};
      
    //SI EL TEXTO ES TP == TOGGLE DE PILOTO DE PRESENCIA  
    }else if(strcmp(texto, "P")== 0){
      presencia = "ON";
      digitalWrite(PINPILOTOPRESENCIA, HIGH);
      
    }else if(strcmp(texto, "N")== 0){
      presencia = "OFF";
      digitalWrite(PINPILOTOPRESENCIA, LOW);
      
    }
    //SI EL TEXTO RECIBIDO NO SON ESOS, SIGNIFICA QUE ES UNA MEDIDA DE TEMPERATURA
    else if(strlen(texto) > 1){
      String txt;
      int punto = 0;
      for(int j = 0; j < strlen(texto);j++){
        if(texto[j]== '.'){punto = j;};
        txt += texto[j];
      }
      //Serial.println(punto);
      //Serial.println(strlen(texto));
      //String temp = txt.substring(13, strlen(texto) - punto);
      String temp = txt.substring(13, punto);
      temperatura = temp;
      //float temp = txt.substring(13.toFloat();
      //double temp = ::atof(txt.c_str());
      //float temp = std::stof(txt);
      //int temp = txt.toInt();
      //double temp = atof(txt.c_str());
      //String r = txt.substr(13, strlen(texto));
      //int p = txt.find(",");
      //p-string+1

      //float p = (float)txt;
       //float p = atof(txt);
       
        
      if(temp.toInt() > 60){
        digitalWrite(PINEXTRACTOR, HIGH);
      }else{
        digitalWrite(PINEXTRACTOR, LOW);
      }
    }
    
    /*if (Serial.available() > 0) {
        
            Serial.print("Temperatura");
            Serial.print(temperatura);      
      }*/
      forUART = "Luces: "+luces+". Presencia: "+presencia+". Temperatura: "+temperatura+"ºC";
      Serial.println(forUART);
      delay(3000);
 
  }

  
   
}
