/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.PostgreSQLTextValueParser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Numeric value parser of PostgreSQL.
 */
public final class PostgreSQLNumericValueParser implements PostgreSQLTextValueParser<Number> {
    
    private static final Map<String, Double> SPECIAL_VALUES = new HashMap<>(7, 1F);
    
    static {
        SPECIAL_VALUES.put("nan", Double.NaN);
        SPECIAL_VALUES.put("infinity", Double.POSITIVE_INFINITY);
        SPECIAL_VALUES.put("+infinity", Double.POSITIVE_INFINITY);
        SPECIAL_VALUES.put("inf", Double.POSITIVE_INFINITY);
        SPECIAL_VALUES.put("+inf", Double.POSITIVE_INFINITY);
        SPECIAL_VALUES.put("-infinity", Double.NEGATIVE_INFINITY);
        SPECIAL_VALUES.put("-inf", Double.NEGATIVE_INFINITY);
    }
    
    @Override
    public Number parse(final String value) {
        Double specialValue = SPECIAL_VALUES.get(value.trim().toLowerCase(Locale.ROOT));
        if (null != specialValue) {
            return specialValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ignored) {
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ignored) {
        }
        return new BigDecimal(value);
    }
}
