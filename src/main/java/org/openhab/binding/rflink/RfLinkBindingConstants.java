/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rflink;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link RfLinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Cyril Cauchois - Initial contribution
 * @author John Jore - Added temperature, X10, X10Secure and RTS/Somfy support
 * @author Marvyn Zalewski - Added humidity support
 */
public class RfLinkBindingConstants {

    public static final String BINDING_ID = "rflink";
    public static final String SERIAL_PORT = "serialPort";
    public static final String BRIDGE_ID = "bridgeId";
    public static final String DEVICE_ID = "deviceId";

    // List of all Bridge Type UIDs
    public static final String BRIDGE_TYPE_MANUAL_BRIDGE = "bridge";
    public final static int BAUD_RATE_DEFAULT = 57600;

    public final static ThingTypeUID BRIDGE_MANUAL = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_MANUAL_BRIDGE);

    public final static String NEW_LINE = "\n";

    /**
     * Presents all supported Bridge types by RFLink binding.
     */
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream.of(BRIDGE_MANUAL).collect(Collectors.toSet()));
    /**
     * Presents all discoverable Bridge types by RFLink binding.
     */
    public final static Set<ThingTypeUID> DISCOVERABLE_BRIDGE_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream.of(BRIDGE_MANUAL).collect(Collectors.toSet()));

    // List of all Channel ids
    public final static String CHANNEL_SHUTTER = "shutter";
    public final static String CHANNEL_COMMAND = "command";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_MOOD = "mood";
    public final static String CHANNEL_SIGNAL_LEVEL = "signalLevel";
    public final static String CHANNEL_DIMMING_LEVEL = "dimmingLevel";
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_HUMIDITY_STATUS = "humidityStatus";
    public final static String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public final static String CHANNEL_LOW_BATTERY = "lowBattery";
    public final static String CHANNEL_PRESSURE = "pressure";
    public final static String CHANNEL_FORECAST = "forecast";
    public final static String CHANNEL_RAIN_RATE = "rainrate";
    public final static String CHANNEL_RAIN_TOTAL = "raintotal";
    public final static String CHANNEL_WIND_DIRECTION = "windDirection";
    public final static String CHANNEL_WIND_SPEED = "windSpeed";
    public final static String CHANNEL_WIND_CHILL = "windChill";
    public final static String CHANNEL_AVERAGE_WIND_SPEED = "averageWindSpeed";
    public final static String CHANNEL_GUST = "gust";
    public final static String CHANNEL_CHILL_FACTOR = "chillFactor";
    public final static String CHANNEL_INSTANT_POWER = "instantPower";
    public final static String CHANNEL_TOTAL_USAGE = "totalUsage";
    public final static String CHANNEL_INSTANT_AMPS = "instantAmps";
    public final static String CHANNEL_TOTAL_AMP_HOURS = "totalAmpHours";
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_MOTION = "motion";
    public final static String CHANNEL_CONTACT = "contact";
    public final static String CHANNEL_VOLTAGE = "voltage";
    public final static String CHANNEL_SET_POINT = "setpoint";
    public final static String CHANNEL_OBSERVATION_TIME = "observationTime";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public final static ThingTypeUID THING_TYPE_ENERGY = new ThingTypeUID(BINDING_ID, "energy");
    public final static ThingTypeUID THING_TYPE_WIND = new ThingTypeUID(BINDING_ID, "wind");
    public final static ThingTypeUID THING_TYPE_RAIN = new ThingTypeUID(BINDING_ID, "rain");
    public final static ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public final static ThingTypeUID THING_TYPE_HUMIDITY = new ThingTypeUID(BINDING_ID, "humidity");
    public final static ThingTypeUID THING_TYPE_RTS = new ThingTypeUID(BINDING_ID, "rts");
    public final static ThingTypeUID THING_TYPE_OREGONTEMPHYGRO = new ThingTypeUID(BINDING_ID, "OregonTempHygro");
    public final static ThingTypeUID THING_TYPE_COLOR = new ThingTypeUID(BINDING_ID, "color");
    public final static ThingTypeUID THING_TYPE_WH1080WEATHERSTATION = new ThingTypeUID(BINDING_ID, "WH1080WeatherStation");

    // Presents all supported Thing types by RFLink binding.
    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_ENERGY,
            THING_TYPE_WIND, THING_TYPE_SWITCH, THING_TYPE_RAIN, THING_TYPE_TEMPERATURE, THING_TYPE_RTS,
            THING_TYPE_HUMIDITY, THING_TYPE_OREGONTEMPHYGRO, THING_TYPE_COLOR, THING_TYPE_WH1080WEATHERSTATION).collect(Collectors.toSet()));
}
