# RFLink binding for OpenHAB 2.0

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
* Wind (_to be tested_)

As the project is at its very beginning, the binding does not support yet commands.

## Discovery

As the project is at its very beginning, the binding does not support yet discovery.

## Configuration

Both bridges and sensor/actuators are easy to configure from the Paper UI. However, a manual configuration looks (thing file) e.g. like

_.things file_
```
Bridge rflink:bridge:usb0 [ serialPort="COM19", baudRate=57600 ] {
    energy myEnergy [ deviceId="Oregon CM119-0004" ]
}
```

_.items file_
```
Number myInstantPower           "Instant Power [%d]"  <chart>   (GroupA) {channel="rflink:energy:usb0:myEnergy:instantPower"}
Number myTotalPower             "Total Power [%d]"  <chart>   (GroupA) {channel="rflink:energy:usb0:myEnergy:totalUsage"}
```

## Dependencies

This binding depends on the following plugins
* org.openhab.io.transport.serial


