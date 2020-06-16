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
package org.openhab.binding.rflink.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.binding.rflink.type.RfLinkTypeUtils;

/**
 * RfLink data class for Somfy/RTS message.
 *
 * @author John Jore - Initial contribution
 * @author Arjan Mels - Added reception and debugged sending
 * @author cartemere - support RollerShutter
 */
public class RfLinkRtsMessage extends RfLinkBaseMessage {
    private static final String KEY_RTS = "RTS";
    private static final Collection<String> KEYS = Arrays.asList(KEY_RTS);

    public Command command = null;
    public State state = null;

    public RfLinkRtsMessage() {
    }

    public RfLinkRtsMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_RTS;
    }

    @Override
    public String toString() {
        String str = "";
        str += super.toString();
        str += ", State = " + state;
        str += ", Command = " + command;
        return str;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);
    }

    @Override
    public Collection<String> keys() {
        return KEYS;
    }

    @Override
    public Map<String, State> getStates() {
        Map<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_SHUTTER, state);
        return map;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command triggeredCommand)
            throws RfLinkNotImpException, RfLinkException {
        super.initializeFromChannel(config, channelUID, triggeredCommand);
        this.command = getCommandAction(channelUID.getId(), triggeredCommand);
        this.state = (State) RfLinkTypeUtils.getOnOffTypeFromType(command);
    }

    @Override
    public String getCommandSuffix() {
        return this.command.toString();
    }

    public Command getCommandAction(String channelId, Type type) throws RfLinkException {
        Command command = null;
        switch (channelId) {
            case RfLinkBindingConstants.CHANNEL_COMMAND:
            case RfLinkBindingConstants.CHANNEL_SHUTTER:
                if (type instanceof StopMoveType) {
                    // STOP action : easy to handle
                    command = StopMoveType.STOP;
                } else {
                    // try to match UP/DOWN switch type from input type
                    Type switchType = RfLinkTypeUtils.getUpDownTypeFromType(type);
                    if (UnDefType.UNDEF.equals(switchType)) {
                        throw new RfLinkException("Channel " + channelId + " does not accept " + type);
                    } else {
                        command = (Command) switchType;
                    }
                }
                break;
            default:
                throw new RfLinkException("Channel " + channelId + " is not relevant here");
        }
        return getEffectiveCommand(command);
    }
}
