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
package org.openhab.binding.rflink.exceptions;

/**
 * Exception for RfLink errors.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkNotImpException extends Exception {

    private static final long serialVersionUID = 5737883000174108158L;

    public RfLinkNotImpException() {
        super();
    }

    public RfLinkNotImpException(String message) {
        super(message);
    }

    public RfLinkNotImpException(String message, Throwable cause) {
        super(message, cause);
    }

    public RfLinkNotImpException(Throwable cause) {
        super(cause);
    }

}
