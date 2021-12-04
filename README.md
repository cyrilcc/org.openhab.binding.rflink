# RFLink binding for OpenHAB 3.1

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
* Rain (_to be tested_)
* RTS / Somfy blinds (Send)
* Temperature (Receive)
* Humidity (Receive) 
* Temperature and Humidity (Receive) 
* Wind (_to be tested_)
* MiLight RGB light (Send/Receive)i* X10 Switch (Send/Receive)
* AB400D Elro Switch (Send)
* X10Secure Contact (Receive)
* Other simple RFLink switches (Send/Receive)


As the project is at its very beginning, the binding does not support many devices.

## Discovery

A first version of discovery is supported, currently depending on the type of device a triggered brand/channel/button will appear in the inbox

## Sending messages

Sending of triggers from openhab -> rflink -> device only works for a few devices.

## Configuration

Bridge config:

| Thing Config | Type    | Description  | Example |
|------------|--------------|--------------|--------------|
| serialPort | String | Path to Device | "/dev/tty.wchusbserial1410" |
| baudRate | Integer | baudRate of the Gateway. Default=57600 | 57600 |
| keepAlivePeriod | Integer | Send "PING" command to the bridge at the specified period. Only enabled if > 0. default=0 | 55 |
| disableDiscovery | Boolean | Enable or disable device Discovery | true |

Thing config:

| Thing Config | Type    | Description  | Example |
|------------|--------------|--------------|--------------|
| deviceId | String | Device Id including protocol and switch number | "X10-01001a-2" |
| isCommandReversed | Boolean | transmit 'opposite' command to the Thing if enabled | true |
| repeats | Integer | number of times to transmit RF messages. default=1 | 1 |


A manual configuration looks like

_.things file_

```
Bridge rflink:bridge:usb0 [ serialPort="COM19", baudRate=57600 ] {
    energy myEnergy [ deviceId="OregonCM119-0004" ]
}
```

most of the time on a raspberry

```
Bridge rflink:bridge:usb0 [ serialPort="/dev/ttyACM0", baudRate=57600, disableDiscovery=true ] {
    energy myEnergy [ deviceId="OregonCM119-0004" ]
}
```

or

```
Bridge rflink:bridge:usb0 [ serialPort="/dev/ttyUSB0", baudRate=57600 ] {
    temperature myTemperature [ deviceId="OregonTemp-0123" ]
    switch      myContact     [ deviceId="X10Secure-12ab-00" ]
    rts         rts-123abc    [ deviceId="RTS-123abc" ]
    switch      x10-01001a-2  [ deviceId="X10-01001a-2" ]
    switch      AB400D-52-2   [ deviceId="AB400D-52-2" ]
    humidity    myHumidity    [ deviceId="AuriolV3-A901" ]
    OregonTempHygro myOregon  [ deviceId="OregonTempHygro-2D60" ]
}
```

All receiving devices must have the protocol as part of the device name (rts, x10 and AB400D).


_.items file_

```
Number myInstantPower "Instant Power [%d]"  <chart> (GroupA) {channel="rflink:energy:usb0:myEnergy:instantPower"}
Number myTotalPower   "Total Power [%d]"    <chart> (GroupA) {channel="rflink:energy:usb0:myEnergy:totalUsage"}
Number oregonTemp     "Oregon Temp [%.2f °C]"                {channel="rflink:temperature:usb0:myTemperature:temperature"}
Number auriolHumidity "Humidity [%d %%]"                     {channel="rflink:humidity:usb0:myHumidity:humidity"}
Rollershutter myBlind "Blind [%s]"                           {channel="rflink:rts:usb0:rts-123abc:command"}
Switch myContact      "Contact [%s]"                         {channel="rflink:switch:usb0:myContact:contact"}
Switch mySwitch       "X10Switch [%s]"                       {channel="rflink:switch:usb0:x10-01001a-2:command"}
Switch myElroSwitch   "AB400DSwitch [%s]"                    {channel="rflink:switch:usb0:AB400D-52-2:command"}
Number temp_outdoor   "Temperature [%.1f °C]"		     {channel="rflink:OregonTempHygro:usb0:myOregon:temperature"}
Number hum_out        "Humidity [%d %%]"		     {channel="rflink:OregonTempHygro:usb0:myOregon:humidity"}
String hstatus_out    "Humidity status [%s]"                 {channel="rflink:OregonTempHygro:usb0:myOregon:humidityStatus" }
Switch low_bat_out    "Low battery [%s]"                     {channel="rflink:OregonTempHygro:usb0:myOregon:lowBattery" }
DateTime obstime_out  "Time of observation [%1$td/%1$tm/%1$tY - %1$tH:%1$tM:%1$tS]"    {channel="rflink:OregonTempHygro:usb0:myOregon:observationTime" }
Color myRGBLight [ deviceId="MiLightv1-C63D-01", repeats=3 ]


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


### Humidity


| Channel ID  | Item Type    | Description  |
|-------------|--------------|--------------|
|   humidity  |   Number     |   Humidity   |


### OregonTempHygro


| Channel ID  | Item Type    | Description  |
|----------------|--------------|--------------|
| temperature    | Number       | Temperature  |
| humidity       | Number       |   Humidity   |
| humidityStatus | String       | Humidity status  |
| lowBattery     | Switch       |   Low battery status   |
| observationTime     | DateTime    |   Last time of observation  (to implement watchdog) |

Humidity status: 

```
Normal (0)
Comfort (1)
Dry (2)
Wet (3)
```

### Switch


| Channel ID  | Item Type    | Description  |
|-------------|--------------|--------------|
| command     | Switch       | Command      |
| contact     | Contact      | Contact state|


### RTS / Somfy


| Channel ID  | Item Type    | Description  |
|-------------|--------------|--------------|
| rts         | Rollershutter| Command      |

### Color


| Channel ID  | Item Type    | Description  |
|-------------|--------------|--------------|
| color       | Color        | Command      |


## Dependencies

This binding depends on the following plugins

* org.openhab.io.transport.serial

From the openHAB shell, just type 

```
feature:install openhab-openhab-core-io-transport-serial-javacomm
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
20;EB;Oregon TempHygro;ID=2D50;TEMP=0013;HUM=77;HSTATUS=3;BAT=LOW;
```

The full protocol reference is available in this [archive](https://drive.google.com/open?id=0BwEYW5Q6bg_ZTDhKQXphN0ZxdEU) 

### How to get sample messages of your Thing

To get sample messages of your Thing, you can enable the DEBUG mode for this binding. 
Add this line to your org.ops4j.pax.logging.cfg (Linux?) file 

 ```
 log4j.logger.org.openhab.binding.rflink = DEBUG
 ```

or add this line to your logback_debug.xml (Windows?) file

 ```
 <logger name="org.openhab.binding.rflink" level="DEBUG" />
 ```

or execute the following command in your Karaf Shell for temporary debug log

 ```
 log:set DEBUG org.openhab.binding.rflink
 ```
 
From OH2.3 the file format has changed and the following two lines must be added:

 ```
 log4j2.logger.org_openhab_binding_rflink.name = org.openhab.binding.rflink
 log4j2.logger.org_openhab_binding_rflink.level = DEBUG
 ```


Or you can use the Serial Monitor of your arduino IDE.

Or you can use the RFLinkLoader application. [See how](http://www.rflink.nl/blog2/development).

### Add your code

1. Add you thing description XML file in the ESH-INF/thing/ directory
2. Implement your message in the org.openhab.binding.rflink.messages package
3. Add the mapping of your new message in the static part of the RfLinkMessageFactory class.
4. Add your new channels names in the RfLinkBindingConstants class
5. Add a ThingTypeUID constant (same class)
6. Add this new constant in the SUPPORTED\_DEVICE\_THING\_TYPES\_UIDS list (same class)
7. To test your thing, don't forget to add you thing in the .things and .items files. See configuration part of this document.
8. Update this README.md document with the new thing and channels you implemented

### How to package your binding

In Eclipse IDE, right click on the pom.xml file, then "Run As", and "Maven Install"  or execute

```
 mvn package
```
