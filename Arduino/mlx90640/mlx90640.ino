#include <M5Stack.h>

/*
  Output the temperature readings to all pixels to be read by a Processing visualizer
  By: Nathan Seidle
  SparkFun Electronics
  Date: May 22nd, 2018
  License: MIT. See license file for more information but you can
  basically do whatever you want with this code.

  Feel like supporting open source hardware?
  Buy a board from SparkFun! https://www.sparkfun.com/products/14769

  This example outputs 768 temperature values as fast as possible. Use this example
  in conjunction with our Processing visualizer.

  This example will work with a Teensy 3.1 and above. The MLX90640 requires some
  hefty calculations and larger arrays. You will need a microcontroller with 20,000
  bytes or more of RAM.

  This relies on the driver written by Melexis and can be found at:
  https://github.com/melexis/mlx90640-library

  Hardware Connections:
  Connect the SparkFun Qwiic Breadboard Jumper (https://www.sparkfun.com/products/14425)
  to the Qwiic board
  Connect the male pins to the Teensy. The pinouts can be found here: https://www.pjrc.com/teensy/pinout.html
  Open the serial monitor at 115200 baud to see the output
*/

#include <Wire.h>

#include "MLX90640_API.h"
#include "MLX90640_I2C_Driver.h"
#define TAMBUFFER 40

const byte MLX90640_address = 0x33; //Default 7-bit unshifted address of the MLX90640

#define TA_SHIFT 8 //Default shift for MLX90640 in open air

float mlx90640To[768];
paramsMLX90640 mlx90640;
float maxTemp;

#include "WiFi.h" 
#include "AsyncUDP.h"

float tf1, tf2, tf3, tf4;
int ppl = 32;
int ppc = 24;

AsyncUDP udp;
const char * ssid = "TEAM_01"; 
const char * password = "123456789";
char texto[TAMBUFFER];

void setup()
{
  Wire.begin();
  Wire.setClock(400000); //Increase I2C clock speed to 400kHz

  Serial.begin(115200); //Fast serial as possible
  
  while (!Serial); //Wait for user to open terminal
  //Serial.println("MLX90640 IR Array Example");

  if (isConnected() == false)
  {
    Serial.println("MLX90640 not detected at default I2C address. Please check wiring. Freezing.");
    while (1);
  }

  //Get device parameters - We only have to do this once
  int status;
  uint16_t eeMLX90640[832];
  status = MLX90640_DumpEE(MLX90640_address, eeMLX90640);
  if (status != 0)
    Serial.println("Failed to load system parameters");

  status = MLX90640_ExtractParameters(eeMLX90640, &mlx90640);
  if (status != 0)
    Serial.println("Parameter extraction failed");

  //Once params are extracted, we can release eeMLX90640 array

  //MLX90640_SetRefreshRate(MLX90640_address, 0x02); //Set rate to 2Hz
  MLX90640_SetRefreshRate(MLX90640_address, 0x03); //Set rate to 4Hz
  //MLX90640_SetRefreshRate(MLX90640_address, 0x07); //Set rate to 64Hz
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
      int i=TAMBUFFER; 
      while (i--) {
        //(texto+i)=(packet.data()+i);
      } 
      //rec=1;        //indica mensaje recibido
    });
  }
  
}

void loop()
{
  //long startTime = millis();
  for (byte x = 0 ; x < 2 ; x++)
  {
    uint16_t mlx90640Frame[834];
    int status = MLX90640_GetFrameData(MLX90640_address, mlx90640Frame);

    float vdd = MLX90640_GetVdd(mlx90640Frame, &mlx90640);
    float Ta = MLX90640_GetTa(mlx90640Frame, &mlx90640);

    float tr = Ta - TA_SHIFT; //Reflected temperature based on the sensor ambient temperature
    float emissivity = 0.95;

    MLX90640_CalculateTo(mlx90640Frame, &mlx90640, emissivity, tr, mlx90640To);
  }
  //long stopTime = millis();
  int pos1 = 0;
  int pos2 = 0;
  int pos3 = 0;
  int pos4 = 0;
  maxTemp = -10;

  
  
  
  for (int x = 0 ; x < 768 ; x++)
  {
    //if(x % 8 == 0) Serial.println();
    Serial.print(mlx90640To[x], 2);
    if(mlx90640To[x] > maxTemp){ maxTemp = mlx90640To[x]; }
    Serial.print(",");
  }
  //delay(1);
  Serial.println("");
  //delay(1);
  
  pos1 = ppl*(ppc/4)+(ppl/4);
  pos2 = ppl*(ppc/4)+(3*ppl/4);
  pos3 = ppl*(3*ppc/4)+(ppl/4);
  pos4 = ppl*(3*ppc/4)+(3*ppl/4);

  tf1 = mlx90640To[pos2];
  tf2 = mlx90640To[pos1];
  tf3 = mlx90640To[pos4];
  tf4 = mlx90640To[pos3];
  
  
  //sprintf (texto, "Temperatura: %f" ,maxTemp);
  //sprintf (texto, "Temperatura: %.0f,%.0f,%.0f,%.0f",tf1, tf2, tf3, tf4);
  sprintf (texto, "%.0f,%.0f,%.0f,%.0f,%.0f",maxTemp,tf1, tf2, tf3, tf4);
  //Serial.println(texto);
  udp.broadcastTo(texto,1234);
}

//Returns true if the MLX90640 is detected on the I2C bus
boolean isConnected()
{
  Wire.beginTransmission((uint8_t)MLX90640_address);
  if (Wire.endTransmission() != 0)
    return (false); //Sensor did not ACK
  return (true);
}
