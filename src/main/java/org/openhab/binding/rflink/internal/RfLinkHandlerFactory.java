/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.rflink.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.handler.RfLinkBridgeHandler;
import org.openhab.binding.rflink.handler.RfLinkHandler;
import org.openhab.binding.rflink.internal.discovery.RfLinkDeviceDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RfLinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Cyril Cauchois - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.rflink", service = ThingHandlerFactory.class)
public class RfLinkHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(RfLinkHandlerFactory.class);
    private final SerialPortManager serialPortManager;

    /**
     * Service registration map
     */
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .concat(RfLinkBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS.stream(),
                    RfLinkBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    @Activate
    public RfLinkHandlerFactory(final @Reference SerialPortManager serialPortManager) {

        logger.debug("RfLinkRxTxConnector()");
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        logger.debug("RfLinkHandlerFactory createHandler({})", thing.getUID().toString());
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (RfLinkBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            RfLinkBridgeHandler handler = new RfLinkBridgeHandler((Bridge) thing, serialPortManager);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (supportsThingType(thingTypeUID)) {
            return new RfLinkHandler(thing);
        } else {
            logger.debug("RfLinkHandlerFactory createHandler() thing is not supported -> returning null");
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (this.discoveryServiceRegs != null) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    private void registerDeviceDiscoveryService(RfLinkBridgeHandler handler) {
        RfLinkDeviceDiscoveryService discoveryService = new RfLinkDeviceDiscoveryService(handler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}
