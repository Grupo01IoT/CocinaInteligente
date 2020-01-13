
#include <stdio.h>
#include <wiringPi.h>

// LED Pin - wiringPi pin 0 is BCM_GPIO 17.

#define	LED	0

int main (void)
{
  printf ("Raspberry Pi blink\n") ;

  wiringPiSetup () ;
  pinMode (LED, OUTPUT) ;


//  digitalWrite (LED, HIGH) ;	// On
//  delay (2000) ;		// mS
  digitalWrite (LED, LOW) ;	// Off
  delay (3300) ;
  digitalWrite (LED, HIGH) ;  // On
//  delay (2000) ;              // mS



  
  return 0 ;
}
