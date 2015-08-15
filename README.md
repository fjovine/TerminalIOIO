# TerminalIOIO
Demo package under AndroidStudio to use the IOIO board as an asynchronous terminal.

Summary
-------
The IOIO board [https://github.com/ytai/ioio](https://github.com/ytai/ioio) is a general purpose USB interface 
board offering a rich set of features implemented by means of a powerful blend of software/firmware/hardware 
completely OpenSource.

There are many possible uses of this board (please refer to the original documentation by **Ytai Ben-Tsvi**, the author
of the IOIO project) but most of the examples are to be loaded and compiled under Eclipse. This one, on the other side, presents a project directly loadable and compilable under Android Studio.

It is an android app using the UART embedded in a IOIO board to implement a simple terminal that can 
send and receive messages on a serial line, useful to test a hardware prototype.

Set-up
-----

The experimental set-up used is as follows.

![h1](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/H1.jpg)


The IOIO board is programmed so as 
* pin 6 of is RX, i.e. receives the serial data
* pin 7 of is TX, i.e. transmits the serial data

These pins (as stated in the IOIO board documentation) are 5V logic, so they are directly connected to the corresponding pins of a USB->Serial interface. Then these lines are sent to a PC (linux) and connected to a serial terminal (in this case gkterm under ubuntu 15.04).

The IOIO board is powered through the GND-5V lines and not through the USB connector (white cable to the right)
For normal usage, the red cable to the right can be directly connected to the USB connector of the Android device (in this case an Android Tablet). During development this connection is troublesome as the same USB port should be used both as connection for ADB (Android Debugger) and to connect the IOIO board.

Theorethically it is possible to route the ADB connection through WiFi, practically this is not simple under Android Studio (currently the development environment officially supported by Google).
Therefore, considering that the IOIO firmware supports transparently both the direct USB connection to the Android device and the indirect connection through a Bluetooth interface, the latter solution has been used during development.

![h2](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/H2.jpg)

Note: if the direct USB connection is used, no additional operations are required. If the Bluetooth interface is needed, prior to any possible usage the Bloototh pairing procedure should be followed.

Usage of the app
----------------

Once installed, the TerminalIOIO App has a standard icon like the one encircled in the following

![desktop](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/S1.png)

Once launched, after a short while, a toast (small dialog box) is shown to prompt the operator.
The screen contains (from top to bottom)
* a line where to insert the message to be sent
* a widget showing the messages sent
* a widget showing the messages received
* the android keyboard featuring the "Send" button

![init_toast](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/S2.png)

![SendAndroid](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/S3.png)

![ReceiveLinux](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/S4.png)

![init_toast](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/S5.png)

![init_toast](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/S6.png)

![init_toast](https://github.com/fjovine/TerminalIOIO/blob/master/docpics/S7.png)
