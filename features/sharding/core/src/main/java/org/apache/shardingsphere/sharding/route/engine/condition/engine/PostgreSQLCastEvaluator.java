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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Optional;

/**
 * Evaluator for PostgreSQL and openGauss {@code CAST} / {@code ::} expressions, used by sharding-condition extraction so
 * that a casted sharding-key value routes on the database-visible cast result rather than the raw bound Java value.
 *
 * <p>The evaluator supports the cast targets that the routing path commonly sees on a sharding key: integer family
 * ({@code int2} / {@code int4} / {@code int8} and their named aliases), arbitrary-precision {@code numeric} /
 * {@code decimal}, floating point ({@code float4} / {@code real} / {@code float8} / {@code double precision}), text
 * family ({@code text} / {@code varchar} / {@code char} / {@code bpchar} / {@code name}) and {@code bool} /
 * {@code boolean}. Casts to other target types or conversions that lose information (parse failure, overflow, fractional
 * value to integer where rounding is undefined) return {@link Optional#empty()} so the caller leaves the cast in place
 * and routes broadcast.</p>
 *
 * <p>Numeric-to-integer rounding follows PostgreSQL's documented {@code ROUND_HALF_EVEN} behavior, e.g. {@code 1.5::int4}
 * is {@code 2} and {@code 2.5::int4} is also {@code 2}. Integer overflow is detected via
 * {@link BigDecimal#intValueExact()} / {@link BigDecimal#longValueExact()} / {@link BigDecimal#shortValueExact()} and
 * surfaces as {@link Optional#empty()}.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLCastEvaluator {
    
    /**
     * Evaluate a cast expression.
     *
     * @param rawValue bound Java value of the inner parameter marker or literal
     * @param castTargetType outermost cast target type name as produced by the parser, e.g. {@code "int4"} or
     *                       {@code "varchar(10)"}
     * @return the database-visible cast result wrapped in {@link Optional}, or empty when the cast target is unsupported
     *         or the conversion fails
     */
    public static Optional<Comparable<?>> evaluate(final Object rawValue, final String castTargetType) {
        if (null == rawValue || null == castTargetType) {
            return Optional.empty();
        }
        String normalized = normalize(castTargetType);
        try {
            switch (categoryOf(normalized)) {
                case INT2:
                    return castToShort(rawValue);
                case INT4:
                    return castToInteger(rawValue);
                case INT8:
                    return castToLong(rawValue);
                case NUMERIC:
                    return castToBigDecimal(rawValue);
                case FLOAT4:
                    return castToFloat(rawValue);
                case FLOAT8:
                    return castToDouble(rawValue);
                case TEXT:
                    return castToText(rawValue);
                case BOOL:
                    return castToBoolean(rawValue);
                default:
                    return Optional.empty();
            }
        } catch (final ArithmeticException | NumberFormatException ignored) {
            return Optional.empty();
        }
    }
    
    private static String normalize(final String castTargetType) {
        String result = castTargetType.toUpperCase(Locale.ROOT).trim();
        int parenIndex = result.indexOf('(');
        return parenIndex > 0 ? result.substring(0, parenIndex).trim() : result;
    }
    
    private static Category categoryOf(final String normalized) {
        switch (normalized) {
            case "INT2":
            case "SMALLINT":
                return Category.INT2;
            case "INT":
            case "INT4":
            case "INTEGER":
                return Category.INT4;
            case "INT8":
            case "BIGINT":
                return Category.INT8;
            case "NUMERIC":
            case "DECIMAL":
            case "DEC":
                return Category.NUMERIC;
            case "FLOAT4":
            case "REAL":
                return Category.FLOAT4;
            case "FLOAT8":
            case "DOUBLE":
            case "DOUBLE PRECISION":
                return Category.FLOAT8;
            case "TEXT":
            case "VARCHAR":
            case "CHARACTER VARYING":
            case "CHAR":
            case "CHARACTER":
            case "BPCHAR":
            case "NAME":
                return Category.TEXT;
            case "BOOL":
            case "BOOLEAN":
                return Category.BOOL;
            default:
                return Category.OTHER;
        }
    }
    
    private static Optional<Comparable<?>> castToShort(final Object value) {
        BigDecimal bigDecimal = toBigDecimal(value);
        return null == bigDecimal ? Optional.empty() : Optional.of(bigDecimal.setScale(0, RoundingMode.HALF_EVEN).shortValueExact());
    }
    
    private static Optional<Comparable<?>> castToInteger(final Object value) {
        BigDecimal bigDecimal = toBigDecimal(value);
        return null == bigDecimal ? Optional.empty() : Optional.of(bigDecimal.setScale(0, RoundingMode.HALF_EVEN).intValueExact());
    }
    
    private static Optional<Comparable<?>> castToLong(final Object value) {
        BigDecimal bigDecimal = toBigDecimal(value);
        return null == bigDecimal ? Optional.empty() : Optional.of(bigDecimal.setScale(0, RoundingMode.HALF_EVEN).longValueExact());
    }
    
    private static Optional<Comparable<?>> castToBigDecimal(final Object value) {
        BigDecimal bigDecimal = toBigDecimal(value);
        return null == bigDecimal ? Optional.empty() : Optional.of(bigDecimal);
    }
    
    private static Optional<Comparable<?>> castToFloat(final Object value) {
        if (value instanceof Boolean) {
            return Optional.empty();
        }
        BigDecimal bigDecimal = toBigDecimal(value);
        return null == bigDecimal ? Optional.empty() : Optional.of(bigDecimal.floatValue());
    }
    
    private static Optional<Comparable<?>> castToDouble(final Object value) {
        if (value instanceof Boolean) {
            return Optional.empty();
        }
        BigDecimal bigDecimal = toBigDecimal(value);
        return null == bigDecimal ? Optional.empty() : Optional.of(bigDecimal.doubleValue());
    }
    
    private static Optional<Comparable<?>> castToText(final Object value) {
        if (value instanceof Boolean) {
            return Optional.of((boolean) value ? "true" : "false");
        }
        if (value instanceof BigDecimal) {
            return Optional.of(((BigDecimal) value).toPlainString());
        }
        return Optional.of(value.toString());
    }
    
    private static Optional<Comparable<?>> castToBoolean(final Object value) {
        if (value instanceof Boolean) {
            return Optional.of((Boolean) value);
        }
        if (value instanceof Number) {
            BigDecimal bigDecimal = toBigDecimal(value);
            return null == bigDecimal ? Optional.empty() : Optional.of(0 != bigDecimal.signum());
        }
        if (value instanceof String) {
            String text = ((String) value).trim().toLowerCase(Locale.ROOT);
            if ("true".equals(text) || "t".equals(text) || "yes".equals(text) || "y".equals(text) || "1".equals(text) || "on".equals(text)) {
                return Optional.of(Boolean.TRUE);
            }
            if ("false".equals(text) || "f".equals(text) || "no".equals(text) || "n".equals(text) || "0".equals(text) || "off".equals(text)) {
                return Optional.of(Boolean.FALSE);
            }
        }
        return Optional.empty();
    }
    
    private static BigDecimal toBigDecimal(final Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }
        if (value instanceof Float || value instanceof Double) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof String) {
            return new BigDecimal(((String) value).trim());
        }
        if (value instanceof Boolean) {
            return (boolean) value ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        return null;
    }
    
    private enum Category {
        INT2, INT4, INT8, NUMERIC, FLOAT4, FLOAT8, TEXT, BOOL, OTHER
    }
}
