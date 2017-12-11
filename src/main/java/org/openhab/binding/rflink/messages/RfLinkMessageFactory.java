/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFLink Message factory
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkMessageFactory {

    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(RfLinkMessageFactory.class);

    private static HashMap<String, Class> mapping = new HashMap<>();
    private static HashMap<ThingTypeUID, Class> thingTypeMapping = new HashMap<>();

    static {
        addMappingOfClass(RfLinkEnergyMessage.class);
        addMappingOfClass(RfLinkWindMessage.class);
        addMappingOfClass(RfLinkRainMessage.class);
        addMappingOfClass(RfLinkSwitchMessage.class);
        addMappingOfClass(RfLinkTemperatureMessage.class);
        addMappingOfClass(RfLinkRtsMessage.class);
    }

    private static void addMappingOfClass(Class _class) {

        try {
            RfLinkMessage m = (RfLinkMessage) _class.newInstance();

            for (String key : m.keys()) {
                mapping.put(key, _class);
            }
            thingTypeMapping.put(m.getThingType(), _class);

        } catch (InstantiationException | IllegalAccessException e) {

        }

    }

    public static RfLinkMessage createMessage(RfLinkBaseMessage message) throws RfLinkException, RfLinkNotImpException {
        String packet = message.rawMessage;
        for (String key : message.values.keySet()) {
            if (mapping.containsKey(key)) {
                try {
                    Class<?> cl = mapping.get(key);
                    Constructor<?> c = cl.getConstructor(String.class);
                    return (RfLinkMessage) c.newInstance(packet);

                } catch (Exception e) {
                    throw new RfLinkException("unable to instanciate message object", e);
                }
            }
        }
        throw new RfLinkNotImpException("No message implementation found for packet " + packet.toString());
    }

    public static RfLinkMessage createMessage(String packet) throws RfLinkException, RfLinkNotImpException {
        return createMessage(new RfLinkBaseMessage(packet) {
        });
    }

    public static RfLinkMessage createMessageForSendingToThing(ThingTypeUID thingType) throws RfLinkException {

        if (thingTypeMapping.containsKey(thingType)) {
            try {
                Class<?> cl = thingTypeMapping.get(thingType);
                Constructor<?> c = cl.getConstructor();
                return (RfLinkMessage) c.newInstance();
            } catch (Exception e) {
                throw new RfLinkException("Unable to instanciate message object", e);
            }
        }
        return null;
    }
}
