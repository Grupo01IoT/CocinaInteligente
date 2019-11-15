/* Blink Example

   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/
#include <stdio.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "driver/gpio.h"
#include "sdkconfig.h"
#include "include/app_main.h"

/* Can use project configuration menu (idf.py menuconfig) to choose the GPIO to blink,
   or you can edit the following line and set a number here.
*/
#define BLINK_GPIO 13

en_fsm_state g_state = WAIT_FOR_WAKEUP;
int g_is_enrolling = 0;
int g_is_deleting = 0;





void gpio_led_init()
{
    gpio_config_t gpio_conf;
    gpio_conf.mode = GPIO_MODE_OUTPUT;
    gpio_conf.pull_up_en = GPIO_PULLUP_ENABLE;
    gpio_conf.intr_type = GPIO_INTR_DISABLE;
    gpio_conf.pin_bit_mask = 1LL << GPIO_LED_RED;
    gpio_config(&gpio_conf);
    gpio_conf.pin_bit_mask = 1LL << GPIO_LED_WHITE;
    gpio_config(&gpio_conf);

}

void led_task(void *arg)
{
    while(1)
    {
        switch (g_state)
        {
            case WAIT_FOR_WAKEUP:
                gpio_set_level(GPIO_LED_RED, 1);
                gpio_set_level(GPIO_LED_WHITE, 0);
                break;

            case WAIT_FOR_CONNECT:
                gpio_set_level(GPIO_LED_WHITE, 0);
                gpio_set_level(GPIO_LED_RED, 1);
                vTaskDelay(1000 / portTICK_PERIOD_MS);
                gpio_set_level(GPIO_LED_RED, 0);
                break;

            case START_DETECT:
            case START_RECOGNITION:
                gpio_set_level(GPIO_LED_WHITE, 1);
                gpio_set_level(GPIO_LED_RED, 0);
                break;

            case START_ENROLL:
                gpio_set_level(GPIO_LED_WHITE, 1);
                gpio_set_level(GPIO_LED_RED, 1);
                break;

            case START_DELETE:
                gpio_set_level(GPIO_LED_WHITE, 1);
                for (int i = 0; i < 3; i++)
                {
                    gpio_set_level(GPIO_LED_RED, 1);
                    vTaskDelay(200 / portTICK_PERIOD_MS);
                    gpio_set_level(GPIO_LED_RED, 0);
                    vTaskDelay(100 / portTICK_PERIOD_MS);
                }
                break;

            default:
                gpio_set_level(GPIO_LED_WHITE, 1);
                gpio_set_level(GPIO_LED_RED, 0);
                break;
        }
        vTaskDelay(1000 / portTICK_PERIOD_MS);
    }
}











void app_main(void)
{
	for (int i = 2; i >= 0; i--) {
		printf("Init in %d seconds...\n", i);
	    vTaskDelay(1000 / portTICK_PERIOD_MS);
	}
	app_speech_wakeup_init();
    xTaskCreatePinnedToCore(&led_task, "blink_task", configMINIMAL_STACK_SIZE, NULL, 5, NULL, 0);
    g_state = WAIT_FOR_WAKEUP;

    vTaskDelay(30 / portTICK_PERIOD_MS);
    //"Please say 'Hi LeXin' to the board";
    while (g_state == WAIT_FOR_WAKEUP){
        vTaskDelay(1000 / portTICK_PERIOD_MS);

    }
}



