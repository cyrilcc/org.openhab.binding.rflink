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
package org.openhab.binding.rflink.config;

/**
 * Configuration class for {@link RflinkBinding} device.
 *
 * @author Cyril Cauchois - Initial contribution
 */

public class RfLinkDeviceConfiguration {

    // Device Id
    public String deviceId;

    // Number of times to repeat a message
    public int repeats;

    // reverse commands on the device
    public boolean isCommandReversed = Boolean.FALSE;

    @Override
    public String toString() {
        return "RfLinkDeviceConfiguration [deviceId=" + deviceId + "]";
    }
}
