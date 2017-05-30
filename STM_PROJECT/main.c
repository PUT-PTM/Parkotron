/* Include core modules */
#include "stm32f4xx.h"
#include "stm32f4xx_usart.h"
#include "stm32f4xx_gpio.h"
#include "stm32f4xx_rcc.h"
#include "stm32f4xx_rtc.h"
#include "stm32f4xx_tim.h"
/* Include my libraries here */
#include "defines.h"
#include "tm_stm32f4_delay.h"
#include "tm_stm32f4_hcsr04.h"
#include <stdio.h>
short temp;
void enable_clocks(void)
{
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE);
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);
}

void set_the_GPIOs()
{
	GPIO_InitTypeDef GPIO_InitStructure; //configuration for usart (AF = alternate function = usart)
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10 | GPIO_Pin_11;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_Init(GPIOC, &GPIO_InitStructure);

	GPIO_InitTypeDef GPIO_InitStructure2; //configuration for blinking with leds
	GPIO_InitStructure2.GPIO_Pin = GPIO_Pin_12 | GPIO_Pin_13 | GPIO_Pin_14;
	GPIO_InitStructure2.GPIO_Mode = GPIO_Mode_OUT;
	GPIO_InitStructure2.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStructure2.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_InitStructure2.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_Init(GPIOD, &GPIO_InitStructure2);

	GPIO_InitTypeDef GPIO_InitStructure3;
	GPIO_InitStructure3.GPIO_Pin = GPIO_Pin_0;
	GPIO_InitStructure3.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStructure3.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStructure3.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_InitStructure3.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_Init(GPIOA, &GPIO_InitStructure3);
}

void config_of_usart()
{
	USART_InitTypeDef USART_InitStructure;
		USART_InitStructure.USART_BaudRate = 9600;
		USART_InitStructure.USART_StopBits = USART_StopBits_1;
		USART_InitStructure.USART_WordLength = USART_WordLength_8b;
		USART_InitStructure.USART_Parity = USART_Parity_No;
		USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
		USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
		USART_Init(USART3, &USART_InitStructure);
		USART_Cmd(USART3, ENABLE);
}

void init_all_sonars(TM_HCSR04_t* HCSR04_1, TM_HCSR04_t* HCSR04_2, TM_HCSR04_t* HCSR04_3, TM_HCSR04_t* HCSR04_4)
{
	if (!TM_HCSR04_Init(HCSR04_1, GPIOD, GPIO_PIN_7, GPIOD, GPIO_PIN_5)) {//loop for sensor 1
			while (1) {
				GPIO_ToggleBits(GPIOD, GPIO_Pin_12);
				Delayms(100);//enter this loop if something is incorrect
			}
		}
	if (!TM_HCSR04_Init(HCSR04_2, GPIOD, GPIO_PIN_6, GPIOD, GPIO_PIN_4)) {//loop for sensor 2
				while (1) {
					GPIO_ToggleBits(GPIOD, GPIO_Pin_12 | GPIO_Pin_13);
					Delayms(100);//enter this loop if something is incorrect
				}
			}

	if (!TM_HCSR04_Init(HCSR04_3, GPIOD, GPIO_PIN_3, GPIOD, GPIO_PIN_1)) {//loop for sensor 3
				while (1) {
					GPIO_ToggleBits(GPIOD, GPIO_Pin_12 | GPIO_Pin_13 | GPIO_Pin_14);
					Delayms(100);//enter this loop if something is incorrect
				}
			}
	if (!TM_HCSR04_Init(HCSR04_4, GPIOD, GPIO_PIN_2, GPIOD, GPIO_PIN_0)) {//loop for sensor 4
				while (1) {
					GPIO_ToggleBits(GPIOD, GPIO_Pin_14);
					Delayms(100);//enter this loop if something is incorrect
				}
			}
}

void build_and_send_the_packet(short measure_1, short measure_2, short measure_3, short measure_4)
{
	char packet[22];
	char buffer[4];

	packet[0] = 'S';


	itoa(measure_1, buffer, 10);      normalise_the_buffer(buffer, measure_1);
	packet[1] =  buffer[1];   packet[2] =  buffer[2];   packet[3] = buffer[3];
	packet[4] = ';';

	itoa(measure_2, buffer, 10); normalise_the_buffer(buffer, measure_2);
	packet[5] =  buffer[1];   packet[6] =  buffer[2];   packet[7] = buffer[3];
	packet[8] = ';';

	itoa(measure_3, buffer, 10);      normalise_the_buffer(buffer, measure_3);
	packet[9] =  buffer[1];   packet[10] =  buffer[2];   packet[11] = buffer[3];
	packet[12] = ';';

	itoa(measure_4, buffer, 10);      normalise_the_buffer(buffer, measure_4);
	packet[13] =  buffer[1];   packet[14] =  buffer[2];   packet[15] = buffer[3];
	packet[16] = ';';

	short checksum = measure_1 + measure_2 + measure_3 + measure_4;
	itoa(checksum,buffer,10);    normalise_the_buffer(buffer, checksum);

	packet[17] =  buffer[0];   packet[18] =  buffer[1];   packet[19] = buffer[2];   packet[20] = buffer[3];

	packet[21] = 'E';

	send_the_packet(packet,22);//sending...
}

void normalise_the_buffer(char* buffer, short digit)
{
	if (digit < 10 && digit >= 1)// CHANGES <digit><digit><digit>0 TO 0<digit><digit><digit>
			{
				buffer[3] = buffer[0];
				buffer[2] = '0';
				buffer[1] = '0';
				buffer[0] = '0';
			}
	if (digit < 100 && digit >= 10)// CHANGES <digit><digit><digit>0 TO 0<digit><digit><digit>
		{
			buffer[3] = buffer[1];
			buffer[2] = buffer[0];
			buffer[1] = '0';
			buffer[0] = '0';
		}
	if (digit < 1000 && digit >= 100)// CHANGES <digit><digit><digit>0 TO 0<digit><digit><digit>
	{
		buffer[3] = buffer[2];
		buffer[2] = buffer[1];
		buffer[1] = buffer[0];
		buffer[0] = '0';
	}
}

void send_the_packet(char* array, short countity)
{
	short i;
	short temp = 0;

	for (i = 0; i < countity; i++) {
		Delayms(40);
		USART_SendData(USART3, array[i]);
		while (USART_GetFlagStatus(USART3, USART_FLAG_TC) == RESET)
			;
	}
}

int main(int argc, char *argv[]) {
	//TM_DELAY_Init();
	SystemInit();

	enable_clocks();
	set_the_GPIOs();

	GPIO_PinAFConfig(GPIOC, GPIO_PinSource10, GPIO_AF_USART3);//configure 2 pins for usart
	GPIO_PinAFConfig(GPIOC, GPIO_PinSource11, GPIO_AF_USART3);

	config_of_usart();

	//configuration of interrupts maybe needed in the future
	/*NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);
	NVIC_EnableIRQ(USART3_IRQn);*/

	TM_HCSR04_t HCSR04_1;
	TM_HCSR04_t HCSR04_2;
	TM_HCSR04_t HCSR04_3;
	TM_HCSR04_t HCSR04_4;

	init_all_sonars(&HCSR04_1, &HCSR04_2, &HCSR04_3, &HCSR04_4);
	//if (!TM_HCSR04_Init(&HCSR04_1, GPIOD, GPIO_PIN_7, GPIOD, GPIO_PIN_5))
	temp = 0;
	while (1) {

		//Distance is returned in cm and also stored in structure
		//You can use both ways

		TM_HCSR04_Read(&HCSR04_1);
		TM_HCSR04_Read(&HCSR04_2);
		TM_HCSR04_Read(&HCSR04_3);
		TM_HCSR04_Read(&HCSR04_4);

		/*if (HCSR04_1.Distance < 0 || HCSR04_2.Distance < 0 || HCSR04_3.Distance < 0 || HCSR04_4.Distance < 0)
		{
			GPIO_SetBits(GPIOD, GPIO_Pin_12 | GPIO_Pin_13 | GPIO_Pin_14);//distance reading error, blink with all leds one long time
			Delayms(800);
			GPIO_ResetBits(GPIOD, GPIO_Pin_12 | GPIO_Pin_13 | GPIO_Pin_14);
		}*/

		if(GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_0))
		{
			temp=1;
			Delayms(250);
		}
		//HCSR04_2.Distance = 20; HCSR04_3.Distance =21; HCSR04_4.Distance = 22;
		if(temp == 1)
		{
		build_and_send_the_packet((short)HCSR04_1.Distance,(short)HCSR04_2.Distance, (short)HCSR04_3.Distance, (short)HCSR04_4.Distance);
		//Delayms(100);//short break for the sensor
		}

	}
}
