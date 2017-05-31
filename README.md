## Overview
The project is a simple version of the parking sensor used in cars nowadays. The STM32 Discovery board measures the distance from 
4 "HC SR-04" sonars, computes it and sends to a mobile app (via Bluetooth), which displays the data in a clear and accessible way.

## DESCRIPTION
The board used for computing is STM32f407vg. The external power supply for the board is the USB bus, however sonars use
the voltage passed through the USART module, because it tends to give a better output, than the 5V pin on the board.
"Parkotron" uses 4 sonars, located on the "bumper", made out of cardboard, every one next to each other, so that they all together 
cover the whole "bumber". A sonar has 4 pins: Vcc, GND, Trig, and Echo. First, it recieves a 10us long, high state on the Trig pin.
Then is sends 8 40KHz impulses and sets the Echo pin high. It waits for the impulses to come back, and sets the Echo pin to low, when they finally do. The period of time, when the Echo pin was set to high, equals the distance the sonar measures in this cycle.
The data gathered from the 4 sonars is packed into a package and sent to the mobile app. The protocol looks like this:
-----------------------------------------------
| S A A A ; B B B ; C C C ; D D D ; X X X X E |
-----------------------------------------------
Where "S" is a starting byte. "A A A" is the measurement from the first sonar (for ex. 0 3 9 equals 39cm). Similarly for the BBB CCC and DDD.
All the 4 measurements are divided by a semicolon ";". "X X X X" is a checksum (a sum of all the 4 measurements computed before sending).
It will later be verified on the mobile app. "E" ends the packet.

Bluetooth HC-05 is a module which has important task - sending data received from sonars to the Application installed on mobile. Module is sending char after char. It's important to set good baud-rate (9600).

Parkotron_app is an application which main task is to receive data sent by the bluetooth module HC-05 and visualize the result. 
Application counts received characters, starting from 'S' to 'E'. If the counter is equal to the 22, which is the length of the packet, 
that means that everything had gone well. Application appends the results of measurements into one String
(A A A ; B B B ; C C C ; D D D ; X X X X), 
splits by ";" into the string array and parses the strings to integers. Now is the time to check checksum. If sum of the AAA + BBB + CCC + DDD equals XXXX then it goes to the
visualization process, otherwise application will display "error" in the fields prepared for measurement results. 
Vizualization process changes the background color of the squares placed below the image of the car. Depending on the values received from sonars, fields will become green if the distance is greater than the set value, or red if the distance is smaller than the set value.


## TOOLS 
STM32 programme wrote in C (CooCox CoIDE Version: 1.7.8). The mobile app was made in Android Studio Version: 2.3.2.

HOW TO RUN 
Download the Android app from the repository. Install it on Android 5.0 or higher.
Plug the USART module to the USB drive. Plug the USB cable to another USB drive and connect it to the board. Pair your android device with
the HC SR-04 module. Pass 1234 as the PIN number. Open your app, connect to device, pick the Bluetooth module. (The red diode should now blink twice every few seconds, that means it's working correctly). IMPORTANT - if anything showed up in the app after connecting, please disconnect, run the app again and connect with the Bluetooth module one more time. There musn't be any "trash" in the bufor. If everything is done correctly, click the blue user button on the board, to start the transmission. If nothing is being sent, then one should try reseting the device or checking the cabling.

## HOW TO COMPILE 
Compile using the CoIDE

## FUTURE IMPROVEMENTS
Sometimes sonars have troubles initiating, a better cabling and a more reliable power supply is needed. The counter in the app (the one that synchronices receiving of the data) sometimes loses the synchronization and the whole transmission has to be restarted. There is sometimes some trash in the receiving bufor, even if nothing has been sent. 

## ATTRIBUTIONS 
Parkotron_app - Application was created based on the sample given by the official Android Developers site. 
https://developer.android.com/samples/BluetoothChat/index.html
Bluetooth is being served by a modified code originally posted 
by:
http://solderer.tv/communication-between-the-stm32-and-android-via-bluetooth/
Sonars are being served by a modified code originally posted by: 
http://stm32f4-discovery.net/2014/08/library-30-measure-distance-hc-sr04-stm32f4xx/

## LICENSE
MIT

## CREDITS
JAKUB KACZMAREK, BARTOSZ KARŁOWSKI, KRZYSZTOF KLAPA, students of Computer Science, Faculty of Electical Engineering, Poznan University of Technology.
The project was conducted during the Microprocessor Lab course held by the Institute of Control and Information Engineering, Poznan University of Technology.
Supervisor: Marek Kraft/Michał Fularz/Tomasz Mańkowski/Adam Bondyra