/**
 *    Keil project for HC-SR04 Ultrasonic distance sensor
 *
 *     Tested on STM32F4-, STM32F429- Discovery and Nucleo F401RE boards
 *
 *    Before you start, select your target, on the right of the "Load" button
 *
 *    @author        Tilen Majerle
 *    @email        tilen@majerle.eu
 *    @website    http://stm32f4-discovery.com
 *    @ide        Keil uVision 5
 *    @packs        STM32F4xx Keil packs version 2.2.0 or greater required
 *    @stdperiph    STM32F4xx Standard peripheral drivers version 1.4.0 or greater required
 */
/* Include core modules */
#include "stm32f4xx.h"
#include "stm32f4xx_gpio.h"
#include "stm32f4xx_rcc.h"
#include "stm32f4xx_rtc.h"
#include "stm32f4xx_tim.h"
/* Include my libraries here */
#include "defines.h"
#include "tm_stm32f4_delay.h"
#include "tm_stm32f4_hcsr04.h"
#include <stdio.h>

int main(void) {
	SystemInit();
    /* HCSR04 Instance */
    TM_HCSR04_t HCSR04;
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

    	GPIO_InitTypeDef  GPIO_InitStructure;
    	/* Configure PD12, PD13, PD14 and PD15 in output pushpull mode */
    	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_12 | GPIO_Pin_13| GPIO_Pin_14;
    	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;
    	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
   	GPIO_Init(GPIOD, &GPIO_InitStructure);


    /* Initialize delay functions */
    TM_DELAY_Init();

    /* Initialize LEDs on board */


    /* Turn on LED red */


    /* Initialize distance sensor1 on pins; ECHO: PD0, TRIGGER: PC1 */
    if (!TM_HCSR04_Init(&HCSR04, GPIOD, GPIO_PIN_9, GPIOC, GPIO_PIN_2)) {
        /* Sensor is not ready to use */
        /* Maybe wiring is incorrect */
        while (1) {
        	GPIO_ToggleBits(GPIOD, GPIO_Pin_12 | GPIO_Pin_13 | GPIO_Pin_14);
            Delayms(100);
        }
    }

    while (1) {
        /* Read distance from sensor 1 */
        /* Distance is returned in cm and also stored in structure */
        /* You can use both ways */
        TM_HCSR04_Read(&HCSR04);

        /* Something is going wrong, maybe incorrect pinout */
        if (HCSR04.Distance < 0) {
            ///sth wrong, blink with all leds
        	GPIO_ToggleBits(GPIOD, GPIO_Pin_12 | GPIO_Pin_13 | GPIO_Pin_14);
        } else if (HCSR04.Distance > 50) {
            /* Distance more than 50cm */
        	GPIO_SetBits(GPIOD, GPIO_Pin_12);
        	GPIO_ResetBits(GPIOD, GPIO_Pin_13);
        } else {
            /* Distance between 0 and 50cm */
        	GPIO_SetBits(GPIOD, GPIO_Pin_13);
        	GPIO_ResetBits(GPIOD, GPIO_Pin_12);
        }

        /* Give some time to sensor */
        Delayms(100);
    }
}
