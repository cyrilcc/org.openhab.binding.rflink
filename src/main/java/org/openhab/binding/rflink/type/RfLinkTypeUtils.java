/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides various services to manipulate & transcode Type objects and their subtypes (Command, State)
 *
 * @author cartemere
 */
public class RfLinkTypeUtils {

    private static Logger logger = LoggerFactory.getLogger(RfLinkTypeUtils.class);
    private static Map<Type, Collection<Type>> TYPE_SYNONYMS_MAP = new HashMap<>();
    private static Map<Type, Type> TYPE_ANTONYM_MAP = new HashMap<>();
    private static Collection<Type> TYPE_ALL = new HashSet<>();
    static {
        // declare synonyms (all Types having the same "meaning")
        declareSynonyms(UpDownType.UP, OnOffType.ON, AllOnOffType.ALLON, OpenClosedType.OPEN,
                IncreaseDecreaseType.INCREASE);
        declareSynonyms(UpDownType.DOWN, OnOffType.OFF, AllOnOffType.ALLOFF, OpenClosedType.CLOSED,
                IncreaseDecreaseType.DECREASE);
        // declare antonyms (opposite operation)
        declareAntonyms(UpDownType.UP, UpDownType.DOWN);
        declareAntonyms(OnOffType.ON, OnOffType.OFF);
        declareAntonyms(AllOnOffType.ALLON, AllOnOffType.ALLOFF);
        declareAntonyms(OpenClosedType.OPEN, OpenClosedType.CLOSED);
        declareAntonyms(IncreaseDecreaseType.INCREASE, IncreaseDecreaseType.DECREASE);
        // declare other supported types (Actions RfLink should be able to handle)
        declareSupportedTypes(StopMoveType.MOVE, StopMoveType.STOP);
    }

    // only used for init
    private static void declareSynonyms(Type... synonyms) {
        Collection<Type> synonymCollection = new ArrayList<Type>();
        for (Type type : synonyms) {
            synonymCollection.add(type);
            TYPE_SYNONYMS_MAP.put(type, synonymCollection);
        }
        declareSupportedTypes(synonyms);
    }

    private static void declareAntonyms(Type firstType, Type antonym) {
        TYPE_ANTONYM_MAP.put(firstType, antonym);
        TYPE_ANTONYM_MAP.put(antonym, firstType);
        TYPE_ALL.add(firstType);
        TYPE_ALL.add(antonym);
    }

    private static void declareSupportedTypes(Type... types) {
        for (Type type : types) {
            TYPE_ALL.add(type);
        }
    }

    public static Type getSynonym(Type inputType, Class expectedOutputClass) {
        Collection<Type> synonyms = TYPE_SYNONYMS_MAP.get(inputType);
        if (synonyms != null) {
            for (Type synonym : synonyms) {
                if (synonym.getClass().equals(expectedOutputClass)) {
                    return synonym;
                }
            }
        }
        return UnDefType.UNDEF;
    }

    public static boolean isSynonym(Type firstType, Type candidate) {
        Type synonym = getSynonym(firstType, candidate.getClass());
        if (candidate.equals(synonym)) {
            return true;
        }
        return false;
    }

    public static Type getAntonym(Type inputType) {
        return TYPE_ANTONYM_MAP.get(inputType);
    }

    public static Type getTypeFromStringValue(String typeValue) {
        for (Type type : TYPE_ALL) {
            if (type.toString().equals(typeValue)) {
                return type;
            }
        }
        return UnDefType.UNDEF;
    }

    /**
     * @param command the command to check
     * @return a UpDownType value (DOWN or UP), except if unable to establish it (in such case : UnDefType.UNDEF)
     */
    public static Type getUpDownTypeFromType(Type command) {
        Type type = UnDefType.UNDEF;
        if (isSynonym(UpDownType.DOWN, command) || isClosePercent(command)) {
            type = UpDownType.DOWN;
        } else if (isSynonym(UpDownType.UP, command) || isOpenPercent(command)) {
            type = UpDownType.UP;
        } else {
            logger.info("Unable to define Switch state from type " + command);
        }
        return type;
    }

    public static Type getOnOffTypeFromType(Type command) {
        Type type = UnDefType.UNDEF;
        if (isSynonym(OnOffType.OFF, command) || isClosePercent(command)) {
            type = OnOffType.OFF;
        } else if (isSynonym(OnOffType.ON, command) || isOpenPercent(command)) {
            type = OnOffType.ON;
        } else {
            logger.info("Unable to define Switch state from type " + command);
        }
        return type;
    }

    public static boolean isClosePercent(Type type) {
        if (type instanceof PercentType) {
            int value = ((PercentType) type).intValue();
            if (value > 50) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOpenPercent(Type type) {
        if (type instanceof PercentType) {
            int value = ((PercentType) type).intValue();
            if (value < 50) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNullOrUndef(Type type) {
        return type == null || UnDefType.UNDEF.equals(type) || UnDefType.NULL.equals(type);
    }

    public static DecimalType boundDecimal(DecimalType inputType, int minValue, int maxValue) {
        DecimalType outputType = null;
        if (inputType != null) {
            if (inputType.intValue() < minValue) {
                outputType = new DecimalType(minValue);
            } else if (inputType.intValue() > maxValue) {
                outputType = new DecimalType(maxValue);
            } else {
                outputType = inputType;
            }
        }
        return outputType;
    }

    /**
     * Convert an input PercentType (0-100%) to a DecimalType within the provided bounds
     *
     * @param inputType a decimalType, if null, return null
     * @param minValue  the min outputValue (i.e. 0%)
     * @param maxValue  the max outputValue (i.e. 100%)
     * @return a DecimalType, result of the conversion of the input PercentType within the bounds
     */
    public static DecimalType toDecimalType(PercentType inputType, int minValue, int maxValue) {
        DecimalType outputType = null;
        if (inputType != null) {
            if (minValue < maxValue) {
                int inputPercentValue = inputType.intValue();
                int inputDecimalValue = ((maxValue - minValue) * inputPercentValue / 100) + minValue;
                outputType = new DecimalType(inputDecimalValue);
            } else {
                throw new IllegalArgumentException(
                        "minValue (" + minValue + ") is not < to maxValue (" + maxValue + ")");
            }
        }
        return outputType;
    }

    public static Command getOnOffCommandFromDimming(DecimalType decimalCommand) {
        Command outputCommand = null;
        if (decimalCommand.intValue() > 0) {
            outputCommand = OnOffType.ON;
        } else {
            outputCommand = OnOffType.OFF;
        }
        return outputCommand;
    }

}
