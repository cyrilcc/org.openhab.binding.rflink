/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.internal.discovery;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RfLinkBridgeDiscovery} is responsible for discovering new RFXCOM
 * transceivers.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Daan Sieben - Modified for RfLink
 *
 */
public class RfLinkBridgeDiscovery extends AbstractDiscoveryService {

    private final static Logger logger = LoggerFactory.getLogger(RfLinkBridgeDiscovery.class);

    /** The refresh interval for background discovery */
    private long refreshInterval = 600;

    private boolean unsatisfiedLinkErrorLogged = false;

    private ScheduledFuture<?> discoveryJob;

    private Runnable discoverRunnable = new Runnable() {

        @Override
        public void run() {
            discoverRfLink();
        }
    };

    public RfLinkBridgeDiscovery() {
        super(RfLinkBindingConstants.DISCOVERABLE_BRIDGE_THING_TYPES_UIDS, 10, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return RfLinkBindingConstants.DISCOVERABLE_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        logger.debug("Start discovery scan for RfLink transceivers");
        discoverRfLink();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start background discovery for RfLink transceivers");
        discoveryJob = scheduler.scheduleAtFixedRate(discoverRunnable, 0, refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop background discovery for RfLink transceivers");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    private synchronized void discoverRfLink() {
        //
        // try {
        // JD2XX jd2xx = new JD2XX();
        // logger.debug("Discovering RFXCOM transceiver devices by JD2XX version {}", jd2xx.getLibraryVersion());
        // String[] devDescriptions = (String[]) jd2xx.listDevicesByDescription();
        // String[] devSerialNumbers = (String[]) jd2xx.listDevicesBySerialNumber();
        // logger.debug("Discovered {} FTDI device(s)", devDescriptions.length);
        //
        // for (int i = 0; i < devSerialNumbers.length; ++i) {
        // if (devDescriptions != null && devDescriptions.length > 0) {
        // switch (devDescriptions[i]) {
        // case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX433:
        // addBridge(RFXComBindingConstants.BRIDGE_RFXTRX443, devSerialNumbers[i]);
        // break;
        // case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX315:
        // addBridge(RFXComBindingConstants.BRIDGE_RFXTRX315, devSerialNumbers[i]);
        // break;
        // case RFXComBindingConstants.BRIDGE_TYPE_RFXREC433:
        // addBridge(RFXComBindingConstants.BRIDGE_RFXREC443, devSerialNumbers[i]);
        // break;
        // default:
        // logger.trace("Ignore unknown device '{}'", devDescriptions[i]);
        // }
        // }
        // }
        //
        // logger.debug("Discovery done");
        //
        // } catch (IOException e) {
        // logger.error("Error occured during discovery", e);
        // } catch (UnsatisfiedLinkError e) {
        // if (unsatisfiedLinkErrorLogged) {
        // logger.debug(
        // "Error occured when trying to load native library for OS '{}' version '{}', processor '{}'",
        // System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"),
        // e);
        // } else {
        // logger.error(
        // "Error occured when trying to load native library for OS '{}' version '{}', processor '{}'",
        // System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"),
        // e);
        // unsatisfiedLinkErrorLogged = true;
        // }
        // }
    }

    private void addBridge(ThingTypeUID bridgeType, String bridgeId) {
        // logger.debug("Discovered RFXCOM transceiver, bridgeType='{}', bridgeId='{}'", bridgeType, bridgeId);
        //
        // Map<String, Object> properties = new HashMap<>(2);
        // properties.put(RFXComBindingConstants.BRIDGE_ID, bridgeId);
        //
        // ThingUID uid = new ThingUID(bridgeType, bridgeId);
        // if (uid != null) {
        // DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
        // .withLabel("RFXCOM transceiver").build();
        // thingDiscovered(result);
        // }

    }
}
