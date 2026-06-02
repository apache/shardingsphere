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
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

/**
 * Evaluator for PostgreSQL and openGauss {@code CAST} / {@code ::} expressions, used by sharding-condition extraction so
 * that a casted sharding-key value routes on the database-visible cast result rather than the raw bound Java value.
 *
 * <p>Cast semantics dispatch on both the source Java type and the target SQL type, mirroring PostgreSQL's
 * {@code pg_cast} catalog where each (source, target) pair binds to its own C function with its own rounding /
 * truncation / overflow rules. Java {@link BigDecimal} routes through the {@code numeric_int*} path with
 * {@link RoundingMode#HALF_UP} (round away from zero) while Java {@link Float} / {@link Double} route through the
 * {@code dtoi4} / {@code ftoi4} path with {@link Math#rint(double)} (banker's rounding, IEEE 754 round-half-to-even);
 * {@code 2.5::numeric::int4} therefore returns {@code 3} while {@code 2.5::float8::int4} returns {@code 2}, matching
 * PostgreSQL 16.</p>
 *
 * <p>Cast targets with a type modifier such as {@code varchar(1)} / {@code numeric(3,1)} return
 * {@link Optional#empty()} because applying the modifier requires character-length / precision-scale semantics the
 * routing path does not model. Naked {@code char} / {@code character} (without typmod, equivalent to
 * {@code character(1)}) truncates to the first character, {@code name} truncates to 63 characters, and {@code text}
 * / {@code varchar} / {@code bpchar} return the value unchanged.</p>
 *
 * <p>Casts that PostgreSQL itself rejects (no entry in {@code pg_cast}) also return {@link Optional#empty()} here so
 * that routing falls through to broadcast and the database-visible failure surfaces at execution time. Examples:
 * {@code bool::numeric}, {@code numeric::bool}, {@code float8::bool}, {@code '1.5'::int4} (PG raises
 * {@code invalid input syntax for type integer}).</p>
 *
 * <p>openGauss inherits its {@code pg_cast} catalog from PostgreSQL 9.2 and keeps the same {@code numeric_int4} /
 * {@code dtoi4} / {@code ftoi4} / {@code bpchar} / {@code namein} cast functions, so the cell-by-cell behavior
 * verified here against PostgreSQL 16 also describes the openGauss path. The evaluator is invoked only when the
 * parser produces a {@code TypeCastExpression}, which is emitted exclusively by the PostgreSQL and openGauss
 * visitors; MySQL / Oracle / SQL Server {@code CAST(...)} syntax becomes a {@code FunctionSegment} and bypasses
 * this evaluator entirely, so there is no cross-dialect contamination risk.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLCastEvaluator {
    
    private static final int NAME_MAX_BYTES = 63;
    
    private static final MathContext FLOAT_TO_NUMERIC_PRECISION = new MathContext(15, RoundingMode.HALF_EVEN);
    
    /**
     * Evaluate a cast expression.
     *
     * @param rawValue bound Java value of the inner parameter marker or literal
     * @param castTargetType outermost cast target type name as produced by the parser, e.g. {@code "int4"} or
     *                       {@code "varchar(10)"}
     * @return the database-visible cast result wrapped in {@link Optional}, or empty when the cast is unsupported or
     *         the conversion fails
     */
    public static Optional<Comparable<?>> evaluate(final Object rawValue, final String castTargetType) {
        if (null == rawValue || null == castTargetType) {
            return Optional.empty();
        }
        if (castTargetType.indexOf('(') >= 0) {
            return Optional.empty();
        }
        Target target = targetOf(castTargetType.toUpperCase(Locale.ROOT).trim());
        Source source = sourceOf(rawValue);
        try {
            return dispatch(rawValue, source, target);
        } catch (final ArithmeticException | NumberFormatException ignored) {
            return Optional.empty();
        }
    }
    
    private static Optional<Comparable<?>> dispatch(final Object value, final Source source, final Target target) {
        switch (target) {
            case INT2:
            case INT4:
            case INT8:
                return castToInteger(value, source, target);
            case NUMERIC:
                return castToNumeric(value, source);
            case FLOAT4:
            case FLOAT8:
                return castToFloat(value, source, target);
            case TEXT:
            case CHAR_NO_TYPMOD:
            case NAME:
                return castToText(value, source, target);
            case BOOL:
                return castToBoolean(value, source);
            default:
                return Optional.empty();
        }
    }
    
    private static Optional<Comparable<?>> castToInteger(final Object value, final Source source, final Target target) {
        switch (source) {
            case BOOL:
                return wrapIntegralBounded((boolean) value ? 1L : 0L, target);
            case INTEGRAL:
                return wrapIntegralBounded(integralLongValue(value), target);
            case NUMERIC:
                return wrapIntegralBounded(((BigDecimal) value).setScale(0, RoundingMode.HALF_UP).longValueExact(), target);
            case FLOAT:
                return wrapFloatToIntegral(((Number) value).doubleValue(), target);
            case STRING:
                return parseStringAsIntegral((String) value, target);
            default:
                return Optional.empty();
        }
    }
    
    private static Optional<Comparable<?>> castToNumeric(final Object value, final Source source) {
        switch (source) {
            case BOOL:
                return Optional.empty();
            case INTEGRAL:
                return value instanceof BigInteger
                        ? Optional.of(new BigDecimal((BigInteger) value))
                        : Optional.of(BigDecimal.valueOf(((Number) value).longValue()));
            case NUMERIC:
                return Optional.of((BigDecimal) value);
            case FLOAT:
                double doubleValue = ((Number) value).doubleValue();
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                    return Optional.empty();
                }
                BigDecimal raw = new BigDecimal(Double.toString(doubleValue), FLOAT_TO_NUMERIC_PRECISION);
                return Optional.of(0 == raw.signum() ? BigDecimal.ZERO : raw.stripTrailingZeros());
            case STRING:
                return Optional.of(new BigDecimal(((String) value).trim()));
            default:
                return Optional.empty();
        }
    }
    
    private static Optional<Comparable<?>> castToFloat(final Object value, final Source source, final Target target) {
        switch (source) {
            case BOOL:
                return Optional.empty();
            case INTEGRAL:
                return wrapDoubleAsFloat(integralToDouble(value), target);
            case NUMERIC:
                return wrapDoubleAsFloat(((BigDecimal) value).doubleValue(), target);
            case FLOAT:
                return wrapDoubleAsFloat(((Number) value).doubleValue(), target);
            case STRING:
                return wrapDoubleAsFloat(Double.parseDouble(((String) value).trim()), target);
            default:
                return Optional.empty();
        }
    }
    
    private static Optional<Comparable<?>> castToText(final Object value, final Source source, final Target target) {
        String text;
        switch (source) {
            case BOOL:
                text = Target.NAME == target ? ((boolean) value ? "t" : "f") : ((boolean) value ? "true" : "false");
                break;
            case INTEGRAL:
                text = value.toString();
                break;
            case NUMERIC:
                text = ((BigDecimal) value).toPlainString();
                break;
            case FLOAT:
                text = formatFloatAsText(((Number) value).doubleValue());
                break;
            case STRING:
                text = (String) value;
                break;
            default:
                return Optional.empty();
        }
        switch (target) {
            case TEXT:
                return Optional.of(text);
            case CHAR_NO_TYPMOD:
                return Optional.of(truncateToFirstCodepoint(text));
            case NAME:
                return Optional.of(truncateToUtf8Bytes(text, NAME_MAX_BYTES));
            default:
                return Optional.empty();
        }
    }
    
    private static String truncateToFirstCodepoint(final String text) {
        if (text.isEmpty()) {
            return text;
        }
        int end = text.offsetByCodePoints(0, 1);
        return text.substring(0, end);
    }
    
    private static String truncateToUtf8Bytes(final String text, final int maxBytes) {
        byte[] utf8 = text.getBytes(StandardCharsets.UTF_8);
        if (utf8.length <= maxBytes) {
            return text;
        }
        int boundary = maxBytes;
        while (boundary > 0 && (utf8[boundary] & 0xC0) == 0x80) {
            boundary--;
        }
        return new String(utf8, 0, boundary, StandardCharsets.UTF_8);
    }
    
    private static Optional<Comparable<?>> castToBoolean(final Object value, final Source source) {
        switch (source) {
            case BOOL:
                return Optional.of((Boolean) value);
            case INTEGRAL:
                return Optional.of(0L != integralLongValue(value));
            case STRING:
                return parseStringAsBoolean((String) value);
            case NUMERIC:
            case FLOAT:
            default:
                return Optional.empty();
        }
    }
    
    private static long integralLongValue(final Object value) {
        return value instanceof BigInteger ? ((BigInteger) value).longValueExact() : ((Number) value).longValue();
    }
    
    private static double integralToDouble(final Object value) {
        return value instanceof BigInteger ? ((BigInteger) value).doubleValue() : (double) ((Number) value).longValue();
    }
    
    private static Optional<Comparable<?>> wrapIntegralBounded(final long longValue, final Target target) {
        switch (target) {
            case INT2:
                return longValue < Short.MIN_VALUE || longValue > Short.MAX_VALUE ? Optional.empty() : Optional.of((short) longValue);
            case INT4:
                return longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE ? Optional.empty() : Optional.of((int) longValue);
            case INT8:
                return Optional.of(longValue);
            default:
                return Optional.empty();
        }
    }
    
    private static Optional<Comparable<?>> wrapFloatToIntegral(final double value, final Target target) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return Optional.empty();
        }
        double rounded = Math.rint(value);
        if (rounded < Long.MIN_VALUE || rounded > Long.MAX_VALUE) {
            return Optional.empty();
        }
        return wrapIntegralBounded((long) rounded, target);
    }
    
    private static Optional<Comparable<?>> wrapDoubleAsFloat(final double value, final Target target) {
        return Target.FLOAT4 == target ? Optional.of((float) value) : Optional.of(value);
    }
    
    private static Optional<Comparable<?>> parseStringAsIntegral(final String text, final Target target) {
        try {
            return wrapIntegralBounded(new BigInteger(text.trim()).longValueExact(), target);
        } catch (final NumberFormatException | ArithmeticException ignored) {
            return Optional.empty();
        }
    }
    
    private static Optional<Comparable<?>> parseStringAsBoolean(final String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "t".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized) || "1".equals(normalized) || "on".equals(normalized)) {
            return Optional.of(Boolean.TRUE);
        }
        if ("false".equals(normalized) || "f".equals(normalized) || "no".equals(normalized) || "n".equals(normalized) || "0".equals(normalized) || "off".equals(normalized)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }
    
    private static String formatFloatAsText(final double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.POSITIVE_INFINITY == value) {
            return "Infinity";
        }
        if (Double.NEGATIVE_INFINITY == value) {
            return "-Infinity";
        }
        return Double.toString(value);
    }
    
    private static Source sourceOf(final Object value) {
        if (value instanceof Boolean) {
            return Source.BOOL;
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte || value instanceof BigInteger) {
            return Source.INTEGRAL;
        }
        if (value instanceof BigDecimal) {
            return Source.NUMERIC;
        }
        if (value instanceof Float || value instanceof Double) {
            return Source.FLOAT;
        }
        if (value instanceof Number) {
            return Source.NUMERIC;
        }
        if (value instanceof String) {
            return Source.STRING;
        }
        return Source.OTHER;
    }
    
    private static Target targetOf(final String normalized) {
        switch (normalized) {
            case "INT2":
            case "SMALLINT":
                return Target.INT2;
            case "INT":
            case "INT4":
            case "INTEGER":
                return Target.INT4;
            case "INT8":
            case "BIGINT":
                return Target.INT8;
            case "NUMERIC":
            case "DECIMAL":
            case "DEC":
                return Target.NUMERIC;
            case "FLOAT4":
            case "REAL":
                return Target.FLOAT4;
            case "FLOAT8":
            case "DOUBLE":
            case "DOUBLE PRECISION":
                return Target.FLOAT8;
            case "TEXT":
            case "VARCHAR":
            case "CHARACTER VARYING":
            case "BPCHAR":
                return Target.TEXT;
            case "CHAR":
            case "CHARACTER":
                return Target.CHAR_NO_TYPMOD;
            case "NAME":
                return Target.NAME;
            case "BOOL":
            case "BOOLEAN":
                return Target.BOOL;
            default:
                return Target.OTHER;
        }
    }
    
    private enum Source {
        INTEGRAL, NUMERIC, FLOAT, STRING, BOOL, OTHER
    }
    
    private enum Target {
        INT2, INT4, INT8, NUMERIC, FLOAT4, FLOAT8, TEXT, CHAR_NO_TYPMOD, NAME, BOOL, OTHER
    }
}
