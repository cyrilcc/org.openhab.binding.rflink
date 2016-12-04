/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.handler.RfLinkBridgeHandler;
import org.openhab.binding.rflink.handler.RfLinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link RfLinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkHandlerFactory extends BaseThingHandlerFactory {

    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(RfLinkHandlerFactory.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(
            RfLinkBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS,
            RfLinkBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        logger.debug("RfLinkHandlerFactory createHandler(" + thing.getUID().toString() + ")");
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (RfLinkBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            RfLinkBridgeHandler handler = new RfLinkBridgeHandler((Bridge) thing);
            // registerDeviceDiscoveryService(handler);
            return handler;
        } else if (supportsThingType(thingTypeUID)) {
            return new RfLinkHandler(thing);
        } else {
            logger.debug("RfLinkHandlerFactory createHandler() thing is not supported -> returning null");
        }

        return null;
    }

    // @Override
    // protected void removeHandler(ThingHandler thingHandler) {
    // if (this.discoveryServiceRegs != null) {
    // ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
    // if (serviceReg != null) {
    // serviceReg.unregister();
    // discoveryServiceRegs.remove(thingHandler.getThing().getUID());
    // }
    // }
    // }
    //
    // private void registerDeviceDiscoveryService(RFXComBridgeHandler handler) {
    // RFXComDeviceDiscoveryService discoveryService = new RFXComDeviceDiscoveryService(handler);
    // discoveryService.activate();
    // this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
    // .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    // }
}
