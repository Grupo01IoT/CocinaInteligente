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
#define PINEXTRACTOR 16
#define PINLUZ 2

#define PINPILOTOPRESENCIA 15
#define TAMBUFFER 60


const char * ssid = "TEAM_01"; 
const char * password = "123456789";
char texto[TAMBUFFER];
boolean rec=0;
AsyncUDP udp;
boolean flag = false;

String temperatura = "20";
String luces = "0";
String presencia = "0";
String extractor = "0";
String forUART = "";
void setup() { 
//  M5.begin(); 
//  M5.Lcd.setTextSize(2); //Tamaño del texto
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
      int i=TAMBUFFER; 
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
  //Serial.println("AAA");
  //Serial.println(texto);
  
  if (Serial.available() > 0) {
    char command = (char) Serial.read();
    //Serial.println("COMANDO recibido - " + command);
    switch (command) {
      case 'O': 
        digitalWrite(PINLUZ , HIGH);   // poner el Pin en HIGH
        flag = true;
        break;
     case 'C':
        digitalWrite(PINLUZ , LOW);    // poner el Pin en LOW
        flag = true;
        break;
      case 'F': 
        digitalWrite(PINEXTRACTOR , HIGH);   // poner el Pin en HIGH
        flag = true;
        break;
     case 'N':
        digitalWrite(PINEXTRACTOR , LOW);    // poner el Pin en LOW
        flag = true;
        break;
     case 'J':
        flag = false;
        break;
     }
  }

  if (!flag){
  
    if (rec){ //Send broadcast  
      rec=0;              //mensaje procesado
      udp.broadcastTo("Recibido",1234);   //envia confirmacion 
      udp.broadcastTo(texto,1234);            //y dato recibido 
      //Serial.println(texto);
      //SI EL TEXTO ES TL == TOGGLE DE LUZ
      //if(strcmp(texto, "L")== 0){
        
        //TOGGLE LED LUCES
        //if(digitalRead(PINLUZ) == HIGH){digitalWrite(PINLUZ, LOW); luces = "OFF";}
        //else if(digitalRead(PINLUZ) == LOW){digitalWrite(PINLUZ, HIGH); luces = "ON";};
        
      //SI EL TEXTO ES TP == TOGGLE DE PILOTO DE PRESENCIA  
      if(strcmp(texto, "P")== 0){
        luces = "1";
        presencia = "1";
        digitalWrite(PINPILOTOPRESENCIA, HIGH);
        digitalWrite(PINLUZ, HIGH);
        
      }else if(strcmp(texto, "N")== 0){
        luces = "0";
        presencia = "0";
        digitalWrite(PINPILOTOPRESENCIA, LOW);
        digitalWrite(PINLUZ, LOW);
      }
      //SI EL TEXTO RECIBIDO NO SON ESOS, SIGNIFICA QUE ES UNA MEDIDA DE TEMPERATURA
    /*else if(strlen(texto) > 1){
        String txt;
        int punto = 0;
        for(int j = 0; j < strlen(texto);j++){
          if(texto[j]== '.'){punto = j;};
          txt += texto[j];
        }
        
        String temp = txt.substring(13, punto);
        temperatura = temp;
        
        //Serial.println(punto);
        //Serial.println(strlen(texto));
        //String temp = txt.substring(13, strlen(texto) - punto);
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
          extractor = "1";
          digitalWrite(PINEXTRACTOR, HIGH);
        }else{
          extractor = "0";
          digitalWrite(PINEXTRACTOR, LOW);
        }
        
          temperatura = temp;
        Serial.println(luces+","+extractor+","+temperatura);
      }*/
      else if(strlen(texto) > 1){
        String txt;
        int posFinalMaxTemp = 0;
        for(int j = 0; j < strlen(texto);j++){
          if(texto[j]==','){
            posFinalMaxTemp = j;
          }
          txt += texto[j];
        }
        //Este -2 es porque salen valores extraños
        String maxtemp = txt.substring(0, posFinalMaxTemp);
        temperatura = txt;
        //Serial.println(temp);
        
        if(maxtemp.toInt() > 60){
          extractor = "1";
          digitalWrite(PINEXTRACTOR, HIGH);
        }else{
          extractor = "0";
          digitalWrite(PINEXTRACTOR, LOW);
        }
      }
        // forUART = "luces:"+luces+",presencia:"+presencia+",temperatura:"+temperatura;
        //Serial.println(forUART);
        Serial.println(luces+","+extractor+","+temperatura+",FinTrama");
        delay(1000);
    }
  }

}
