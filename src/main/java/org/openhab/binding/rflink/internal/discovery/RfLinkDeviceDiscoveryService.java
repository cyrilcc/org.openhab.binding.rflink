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
package org.openhab.binding.rflink.internal.discovery;

import java.util.Set;

import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.handler.RfLinkBridgeHandler;
import org.openhab.binding.rflink.internal.DeviceMessageListener;
import org.openhab.binding.rflink.messages.RfLinkBaseMessage;
import org.openhab.binding.rflink.messages.RfLinkMessage;
import org.openhab.binding.rflink.messages.RfLinkMessageFactory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RfLinkDeviceDiscoveryService} class is used to discover RfLink
 * devices that send messages to RfLink bridge.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Daan Sieben - Modified for RfLink
 * @author Marvyn Zalewski - Added the ability to ignore discoveries
 */
public class RfLinkDeviceDiscoveryService extends AbstractDiscoveryService implements DeviceMessageListener {

    private final Logger logger = LoggerFactory.getLogger(RfLinkDeviceDiscoveryService.class);

    private RfLinkBridgeHandler bridgeHandler;

    public RfLinkDeviceDiscoveryService(RfLinkBridgeHandler rflinkBridgeHandler) {
        super(null, 1, false);
        this.bridgeHandler = rflinkBridgeHandler;
    }

    public void activate() {
        bridgeHandler.registerDeviceStatusListener(this);
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterDeviceStatusListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return RfLinkBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        // this can be ignored here as we discover devices from received messages
    }

    @Override
    public void onDeviceMessageReceived(ThingUID bridge, RfLinkMessage message) {
        logger.debug("Received: bridge: {} message: {}", bridge, message);

        try {
            RfLinkMessage msg = RfLinkMessageFactory.createMessage((RfLinkBaseMessage) message);
            String id = message.getDeviceId();

            ThingTypeUID uid = msg.getThingType();
            ThingUID thingUID = new ThingUID(uid, bridge, id.replace(RfLinkBaseMessage.ID_DELIMITER, "_"));
            if (!bridgeHandler.getConfiguration().disableDiscovery) {
                logger.trace("Adding new RfLink {} with id '{}' to smarthome inbox", thingUID, id);
                String deviceType = msg.getDeviceName();
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(deviceType)
                        .withProperty(RfLinkBindingConstants.DEVICE_ID, msg.getDeviceId()).withBridge(bridge).build();
                thingDiscovered(discoveryResult);
            } else {
                logger.trace("Ignoring RfLink {} with id '{}' - discovery disabled", thingUID, id);
            }
        } catch (Exception e) {
            logger.debug("Error occured during device discovery", e);
        }
    }
}
