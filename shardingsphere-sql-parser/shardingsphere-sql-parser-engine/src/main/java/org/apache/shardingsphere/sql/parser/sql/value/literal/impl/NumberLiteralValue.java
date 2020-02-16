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

package org.apache.shardingsphere.sql.parser.sql.value.literal.impl;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.value.literal.LiteralValue;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Number literal value.
 */
@Getter
public final class NumberLiteralValue implements LiteralValue<Number> {
    
    private final Number value;
    
    public NumberLiteralValue(final String value) {
        this.value = getNumber(value);
    }
    
    private Number getNumber(final String value) {
        try {
            return getBigInteger(value);
        } catch (final NumberFormatException ex) {
            // TODO make sure with double and float
            return new BigDecimal(value);
        }
    }
    
    private static Number getBigInteger(final String value) {
        BigInteger result = new BigInteger(value);
        if (result.compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) <= 0) {
            return result.intValue();
        }
        if (result.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
            return result.longValue();
        }
        return result;
    }
}
