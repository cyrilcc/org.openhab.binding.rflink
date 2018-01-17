/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
