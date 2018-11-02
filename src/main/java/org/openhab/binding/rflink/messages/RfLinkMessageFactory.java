/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFLink Message factory
 *
 * @author Cyril Cauchois - Initial contribution
 * @author Arjan Mels - Order of added keys is retained and search form first to last (to allow overlapping keywords to
 *         be handled properly)
 */
public class RfLinkMessageFactory {

    private static Logger logger = LoggerFactory.getLogger(RfLinkMessageFactory.class);

    private static LinkedHashMap<String, Class> KEY_TO_CLASS = new LinkedHashMap<>();
    private static HashMap<ThingTypeUID, Class> THINGTYPE_TO_CLASS = new HashMap<>();

    /**
     * Mapping of the various message classes.
     * Note that the order is important: first matching class will be used
     */
    static {
        addMappingOfClass(RfLinkEnergyMessage.class);
        addMappingOfClass(RfLinkWindMessage.class);
        addMappingOfClass(RfLinkRainMessage.class);
        addMappingOfClass(RfLinkColorMessage.class);
        addMappingOfClass(RfLinkTemperatureMessage.class);
        addMappingOfClass(RfLinkRtsMessage.class);
        addMappingOfClass(RfLinkHumidityMessage.class);
        addMappingOfClass(RfLinkOregonTempHygroMessage.class);
        addMappingOfClass(RfLinkSwitchMessage.class); // Switch class last as it is most generic
    }

    private static void addMappingOfClass(Class _class) {

        try {
            RfLinkMessage m = (RfLinkMessage) _class.newInstance();

            for (String key : m.keys()) {
                KEY_TO_CLASS.put(key, _class);
            }
            THINGTYPE_TO_CLASS.put(m.getThingType(), _class);

        } catch (InstantiationException | IllegalAccessException e) {

        }

    }

    public static RfLinkMessage createMessage(RfLinkBaseMessage message) throws RfLinkException, RfLinkNotImpException {
        String packet = message.rawMessage;
        for (String key : KEY_TO_CLASS.keySet()) {
            if (message.values.containsKey(key)) {
                try {
                    Class<?> cl = KEY_TO_CLASS.get(key);
                    Constructor<?> c = cl.getConstructor(String.class);
                    return (RfLinkMessage) c.newInstance(packet);
                } catch (Exception e) {
                    logger.error("Exception: {}", e);
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
        if (THINGTYPE_TO_CLASS.containsKey(thingType)) {
            try {
                Class<?> cl = THINGTYPE_TO_CLASS.get(thingType);
                Constructor<?> c = cl.getConstructor();
                return (RfLinkMessage) c.newInstance();
            } catch (Exception e) {
                throw new RfLinkException("Unable to instanciate message object", e);
            }
        }
        return null;
    }
}
