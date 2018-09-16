/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.config;

/**
 * Configuration class for {@link RfLinkBinding} device.
 *
 * @author Cyril Cauchois - Initial contribution
 */

public class RfLinkDeviceConfiguration {

    // Device Id
    public String deviceId;

    // Number of times to repeat a message
    public int repeats;

}
