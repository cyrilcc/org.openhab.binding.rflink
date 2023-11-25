/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rflink.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RfLink data class for Color light message. (e.g. MiLight, Philips Living Color, Ikea Ansluta)
 * (Only tested with MiLight)
 *
 * @author Arjan Mels - Initial contribution
 */
public class RfLinkColorMessage extends RfLinkBaseMessage {
    private Logger logger = LoggerFactory.getLogger(RfLinkColorMessage.class);

    private static final String KEY_RGBW = "RGBW";
    private static final String KEY_SWITCH = "SWITCH";
    private static final String KEY_CMD = "CMD";
    private static final Collection<String> KEYS = Arrays.asList(KEY_RGBW);

    // angle between OpenHab Hue and RFLink/MiLight Hue
    private static final int COLOR_OFFSET = 45;

    private Command command = null;
    private HSBType stateColor = null;
    private OnOffType stateOnOff = null;

    private static HashMap<String, HSBType> currentState = new HashMap<>();

    public RfLinkColorMessage() {
    }

    public RfLinkColorMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_COLOR;
    }

    @Override
    public String toString() {
        String str = "";
        str += super.toString();
        str += ", StateColor = " + stateColor;
        str += ", StateOnOff = " + stateOnOff;
        str += ", Command = " + command;
        return str;
    }

    @Override
    public void encodeMessage(String data) {
        logger.debug("Color Encode data: [{}]", data);
        super.encodeMessage(data);

        if (values.containsKey(KEY_SWITCH)) {
            this.deviceId += ID_DELIMITER + values.get(KEY_SWITCH);
        }

        if (values.containsKey(KEY_RGBW)) {
            String rgbw = values.get(KEY_RGBW);
            int color = Integer.parseInt(rgbw.substring(0, 2), 16);
            int brightness = Integer.parseInt(rgbw.substring(2, 4), 16);
            stateColor = new HSBType(new DecimalType(((color * 360 / 255) + 360 - COLOR_OFFSET) % 360),
                    new PercentType(100), new PercentType(brightness * 100 / 255));
        }
        currentState.put(this.deviceId, stateColor);

        if (values.containsKey(KEY_CMD)) {
            switch (values.get(KEY_CMD)) {
                case "ALLON":
                    stateOnOff = OnOffType.ON;
                    if (stateColor != null) {
                        stateColor = new HSBType(stateColor.getHue(), new PercentType(0), stateColor.getBrightness());
                    }
                    break;
                case "ALLOFF":
                case "ON":
                    stateOnOff = OnOffType.ON;
                    break;
                case "OFF":
                    stateOnOff = OnOffType.OFF;
                    break;
                case "BRIGHT":
                case "COLOR":
                    stateColor = new HSBType(stateColor.getHue(), new PercentType(100), stateColor.getBrightness());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Collection<String> keys() {
        return KEYS;
    }

    @Override
    public Map<String, State> getStates() {
        logger.debug("Color State Requested: [{}]", stateColor);

        Map<String, State> map = new HashMap<>();
        if (stateColor == null) {
            map.put(RfLinkBindingConstants.CHANNEL_COLOR, null);
        } else if (stateOnOff == null || stateOnOff.equals(OnOffType.OFF)) {
            map.put(RfLinkBindingConstants.CHANNEL_COLOR,
                    new HSBType(stateColor.getHue(), stateColor.getSaturation(), new PercentType(0)));
        } else {
            map.put(RfLinkBindingConstants.CHANNEL_COLOR, stateColor);
        }
        return map;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command triggeredCommand)
            throws RfLinkNotImpException, RfLinkException {
        super.initializeFromChannel(config, channelUID, triggeredCommand);

        logger.debug("Color initializeFromChannel: deviceid={}, state={}, class={}, command={}", this.deviceId,
                currentState.get(this.deviceId), triggeredCommand.getClass().getSimpleName(), triggeredCommand);

        command = triggeredCommand;

        stateColor = currentState.get(this.deviceId);
        if (command instanceof HSBType) {
            // HSBType is subclass of PercentType, so must handle before PercentType
            logger.debug("Color initializeFromChannel: HSB command={}", command);
            stateColor = (HSBType) command;
        } else if (command instanceof PercentType) {
            logger.debug("Color initializeFromChannel: Percent command={}", command);
            if (stateColor == null) {
                stateColor = new HSBType(new DecimalType(0), new PercentType(0),
                        new PercentType(((PercentType) command).intValue()));
            } else {
                stateColor = new HSBType(stateColor.getHue(), stateColor.getSaturation(),
                        new PercentType(((PercentType) command).intValue()));
            }
        } else if (command instanceof IncreaseDecreaseType) {
            logger.debug("Color initializeFromChannel: IncDec command={}", command);
            if (stateColor == null) {
                if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    stateColor = new HSBType(new DecimalType(0), new PercentType(0), new PercentType(0));
                } else {
                    stateColor = new HSBType(new DecimalType(0), new PercentType(0), new PercentType(100));
                }
            } else {
                int newValue = stateColor.getBrightness().intValue();
                if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    newValue = Math.max(newValue - 10, 0);
                } else {
                    newValue = Math.min(newValue + 10, 100);
                }
                stateColor = new HSBType(stateColor.getHue(), stateColor.getSaturation(), new PercentType(newValue));
            }
        } else {
            logger.debug("Color initializeFromChannel: Other command={}", command);
        }
        currentState.put(this.deviceId, stateColor);
        logger.debug("Color initializeFromChannel: state={}", stateColor);
    }

    @Override
    public Collection<String> buildMessages() {
        logger.debug("Color decodeMessage: command={}, stateColor={}, stateOnOff={}", command, stateColor, stateOnOff);

        if (command == null) {
            return null;
        }

        String rgbw = null;
        if (stateColor == null) {
            stateColor = new HSBType(new DecimalType(0), new PercentType(0), new PercentType(100));
        }

        logger.debug("Color decodeMessage: color H={}, S={}, B={}", stateColor.getHue(), stateColor.getSaturation(),
                stateColor.getBrightness());

        rgbw = String.format("%02X%02X", ((stateColor.getHue().intValue() + COLOR_OFFSET) % 360) * 255 / 360,
                (int) (stateColor.getBrightness().floatValue() * 255 / 100));

        String cmdString = null;
        boolean sendBright = false;

        if (command instanceof OnOffType && command.equals(OnOffType.OFF)) {
            cmdString = "OFF";
        } else if (stateColor.getBrightness().intValue() == 0) {
            cmdString = "OFF";
        } else if (stateColor.getSaturation().intValue() < 25) {
            cmdString = "ALLON";
            sendBright = true;
        } else if (command instanceof HSBType) {
            // HSBType is subclass of PercentType, so must handle before PercentType
            cmdString = "COLOR";
            sendBright = true;
        } else if (command instanceof PercentType || command instanceof IncreaseDecreaseType) {
            cmdString = "BRIGHT";
        } else {
            cmdString = "ON";
        }

        Collection<String> messages = new ArrayList<String>();
        messages.add(super.buildMessage(rgbw + ";" + cmdString));
        if (sendBright) {
            messages.add(super.buildMessage(rgbw + ";BRIGHT"));
        }
        return messages;
    }
}
