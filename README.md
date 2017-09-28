# RFLink binding for OpenHAB 2.0

[![Build Status](https://travis-ci.org/cyrilcc/org.openhab.binding.rflink.svg?branch=master)](https://travis-ci.org/cyrilcc/org.openhab.binding.rflink)

This repository contains a binding for [OpenHAB 2.0](https://github.com/openhab/openhab-distro) that deals with [RFLink Gateway](http://www.nemcon.nl/blog2/).

This binding is inspired by the [Rfxcom binding](https://github.com/openhab/openhab2-addons/tree/master/addons/binding/org.openhab.binding.rfxcom)

RFLink gateway supports RF 433 Mhz protocols like: 
* Cresta 
* La Crosse
* OWL
* Oregon
* ...

The official supported devices list is available here : [http://www.nemcon.nl/blog2/2015/07/devlist](http://www.nemcon.nl/blog2/2015/07/devlist)

## Supported Things

RFLink binding currently supports following types of devices:

* Energy
* Lighting switch
* Wind (_to be tested_)
* Rain (_to be tested_)
* Temperature
* Switch / Contact

As the project is at its very beginning, the binding does not support yet commands.

## Discovery

A first version of discovery is supported, currently depending on the type of device a triggered brand/channel/button will appear in the inbox

## Sending messages

Sending of triggers from openhab -> rflink -> device does not work yet.

## Configuration

A manual configuration looks like

_.things file_
```
Bridge rflink:bridge:usb0 [ serialPort="COM19", baudRate=57600 ] {
    energy myEnergy [ deviceId="Oregon CM119-0004" ]
}
```

most of the time on a raspberry
```
Bridge rflink:bridge:usb0 [ serialPort="/dev/ttyACM0", baudRate=57600 ] {
    energy myEnergy [ deviceId="OregonCM119-0004" ]
}
```
or
```
Bridge rflink:bridge:usb0 [ serialPort="/dev/ttyUSB0", baudRate=57600 ] {
    temperature myTemperature [ deviceId="OregonTemp-0710" ]
    switch myContact [ deviceId="X10Secure-12ab-00" ]
}
```

_.items file_
```
Number myInstantPower "Instant Power [%d]"  <chart> (GroupA) {channel="rflink:energy:usb0:myEnergy:instantPower"}
Number myTotalPower   "Total Power [%d]"    <chart> (GroupA) {channel="rflink:energy:usb0:myEnergy:totalUsage"}
Number oregonTemp     "Oregon Temp [%.2f °C]"                {channel="rflink:temperature:usb0:myTemperature:temperature"}
Switch myContact      "Contact [%s]"                         {channel="rflink:switch:usb0:myContact:command"}
```

## Supported Channels

### Energy


| Channel ID | Item Type    | Description  |
|------------|--------------|--------------|
| instantPower | Number | Instant power consumption in Watts. |
| totalUsage | Number | Used energy in Watt hours. |
| instantAmp | Number | Instant current in Amperes. |
| totalAmpHours | Number | Used "energy" in ampere-hours. |


### Wind


| Channel ID | Item Type    | Description  |
|------------|--------------|--------------|
| windSpeed | Number | Wind speed in km per hour. |
| windDirection | Number | Wind direction in degrees. |
| averageWindSpeed | Number | Average wind speed in km per hour. |
| windGust | Number | Wind gust speed in km per hour. |
| windChill | Number | Wind temperature in celcius degrees. |


### Rain


| Channel ID | Item Type    | Description  |
|------------|--------------|--------------|
| rainTotal  | Number       | Total rain in millimeters. |
| rainRate   | Number       | Rain fall rate in millimeters per hour. |


### Temperature


| Channel ID  | Item Type    | Description  |
|-------------|--------------|--------------|
| temperature | Number       | Temperature  |


### Switch


| Channel ID  | Item Type    | Description  |
|-------------|--------------|--------------|
| switch      | Switch       | Command      |


## Dependencies

This binding depends on the following plugins
* org.openhab.io.transport.serial

From the openHAB shell, just type 
```
feature:install openhab-transport-serial
```

Or if you are developing your plugin with Eclipse IDE, select Run / Run Configurations... then select openHAB_Runtime click on the plug-ins tab, and check org.openhab.io.transport.serial in the target platform section.


The error message "Unresolved requirement: Import-Package: gnu.io" is a good indicator to know if you miss this dependency.


## How to implement a new Thing

RFLink message are very simple ';' separated strings.

### Packet structure - Received data from RF

Old format:
```
20;ID=9999;Name;LABEL=data;
```
New Format:
```
20;FF;Protocol;ID=9999;LABEL=data;
```

* 20          => Node number 20 means from the RFLink Gateway to the master, 10 means from the master to the RFLink Gateway
* ;           => field separator
* FF          => packet sequence number
* Protocol    => Protocol
* ID          => ID to use in Things file
* LABEL=data  => contains the field type and data for that field, can be present multiple times per device


### Examples

```
20;6A;UPM/Esic;ID=1002;WINSP=0041;WINDIR=5A;BAT=OK;
20;47;Cresta;ID=8001;WINDIR=0002;WINSP=0060;WINGS=0088;WINCHL=b0;
20;0B;Oregon Temp;ID=0710;TEMP=00a8;BAT=LOW;
```

The full protocol reference is available in this [archive](https://drive.google.com/open?id=0BwEYW5Q6bg_ZTDhKQXphN0ZxdEU) 

### How to get sample messages of your Thing

To get sample messages of your Thing, you can enable the DEBUG mode for this binding. 
Add this line to your org.ops4j.pax.logging.cfg file
 ```
 log4j.logger.org.openhab.binding.rflink = DEBUG
 ```

Or you can use the Serial Monitor of your arduino IDE.

Or you can use the RFLinkLoader application. [See how](http://www.nemcon.nl/blog2/2015/07/cc).

### Add your code

1. Add you thing description XML file in the ESH-INF/thing/ directory
2. Implement your message in the org.openhab.binding.rflink.messages package
3. Add the mapping of your new message in the static part of the RfLinkMessageFactory class
4. Add your new channels names in the RfLinkBindingConstants class
5. Add a ThingTypeUID constant (same class)
6. Add this new constant in the SUPPORTED\_DEVICE\_THING\_TYPES\_UIDS list (same class)
7. To test your thing, don't forget to add you thing in the .things and .items files. See configuration part of this document.
8. Update this README.md document with the new thing and channels you implemented

### How to package your binding

In Eclipse IDE, right click on the pom.xml file, then "Run As", and "Maven Install" 

