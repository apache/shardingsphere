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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Safe number operation utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SafeNumberOperationUtil {
    
    /**
     * Execute range intersection method by safe mode.
     *
     * @param range range
     * @param connectedRange connected range
     * @return the intersection result of two ranges
     */
    public static Range<Comparable<?>> safeIntersection(final Range<Comparable<?>> range, final Range<Comparable<?>> connectedRange) {
        try {
            return range.intersection(connectedRange);
        } catch (final ClassCastException ex) {
            Class<?> clazz = getRangeTargetNumericType(range, connectedRange);
            if (null == clazz) {
                throw ex;
            }
            Range<Comparable<?>> newRange = createTargetNumericTypeRange(range, clazz);
            Range<Comparable<?>> newConnectedRange = createTargetNumericTypeRange(connectedRange, clazz);
            return newRange.intersection(newConnectedRange);
        }
    }
    
    /**
     * Execute range closed method by safe mode.
     *
     * @param lowerEndpoint lower endpoint
     * @param upperEndpoint upper endpoint
     * @return new range
     */
    public static Range<Comparable<?>> safeClosed(final Comparable<?> lowerEndpoint, final Comparable<?> upperEndpoint) {
        try {
            return Range.closed(lowerEndpoint, upperEndpoint);
        } catch (final ClassCastException ex) {
            Optional<Class<?>> clazz = getTargetNumericType(Arrays.asList(lowerEndpoint, upperEndpoint));
            if (!clazz.isPresent()) {
                throw ex;
            }
            return Range.closed(parseNumberByClazz(lowerEndpoint.toString(), clazz.get()), parseNumberByClazz(upperEndpoint.toString(), clazz.get()));
        }
    }
    
    /**
     * Execute range contains method by safe mode.
     *
     * @param range range
     * @param endpoint endpoint
     * @return whether the endpoint is included in the range
     */
    public static boolean safeContains(final Range<Comparable<?>> range, final Comparable<?> endpoint) {
        try {
            return range.contains(endpoint);
        } catch (final ClassCastException ex) {
            Comparable<?> rangeUpperEndpoint = range.hasUpperBound() ? range.upperEndpoint() : null;
            Comparable<?> rangeLowerEndpoint = range.hasLowerBound() ? range.lowerEndpoint() : null;
            Optional<Class<?>> clazz = getTargetNumericType(Arrays.asList(rangeLowerEndpoint, rangeUpperEndpoint, endpoint));
            if (!clazz.isPresent()) {
                throw ex;
            }
            Range<Comparable<?>> newRange = createTargetNumericTypeRange(range, clazz.get());
            return newRange.contains(parseNumberByClazz(endpoint.toString(), clazz.get()));
        }
    }
    
    /**
     * Execute range equals method by safe mode.
     *
     * @param sourceRange source range
     * @param targetRange target range
     * @return whether the source range and target range are same
     */
    public static boolean safeRangeEquals(final Range<Comparable<?>> sourceRange, final Range<Comparable<?>> targetRange) {
        Class<?> clazz = getRangeTargetNumericType(sourceRange, targetRange);
        if (null == clazz) {
            return sourceRange.equals(targetRange);
        }
        Range<Comparable<?>> newSourceRange = createTargetNumericTypeRange(sourceRange, clazz);
        Range<Comparable<?>> newTargetRange = createTargetNumericTypeRange(targetRange, clazz);
        return newSourceRange.equals(newTargetRange);
    }
    
    /**
     * Execute collection equals method by safe mode.
     *
     * @param sources source collection
     * @param targets target collection
     * @return whether the element in source collection and target collection are all same
     */
    public static boolean safeCollectionEquals(final Collection<Comparable<?>> sources, final Collection<Comparable<?>> targets) {
        List<Comparable<?>> all = new ArrayList<>(sources);
        all.addAll(targets);
        Optional<Class<?>> clazz = getTargetNumericType(all);
        if (!clazz.isPresent()) {
            return sources.equals(targets);
        }
        List<Comparable<?>> sourceClasses = sources.stream().map(each -> parseNumberByClazz(each.toString(), clazz.get())).collect(Collectors.toList());
        List<Comparable<?>> targetClasses = targets.stream().map(each -> parseNumberByClazz(each.toString(), clazz.get())).collect(Collectors.toList());
        return sourceClasses.equals(targetClasses);
    }
    
    private static Class<?> getRangeTargetNumericType(final Range<Comparable<?>> sourceRange, final Range<Comparable<?>> targetRange) {
        Comparable<?> sourceRangeLowerEndpoint = sourceRange.hasLowerBound() ? sourceRange.lowerEndpoint() : null;
        Comparable<?> sourceRangeUpperEndpoint = sourceRange.hasUpperBound() ? sourceRange.upperEndpoint() : null;
        Comparable<?> targetRangeLowerEndpoint = targetRange.hasLowerBound() ? targetRange.lowerEndpoint() : null;
        Comparable<?> targetRangeUpperEndpoint = targetRange.hasUpperBound() ? targetRange.upperEndpoint() : null;
        return getTargetNumericType(Arrays.asList(sourceRangeLowerEndpoint, sourceRangeUpperEndpoint, targetRangeLowerEndpoint, targetRangeUpperEndpoint)).orElse(null);
    }
    
    private static Range<Comparable<?>> createTargetNumericTypeRange(final Range<Comparable<?>> range, final Class<?> clazz) {
        if (range.hasLowerBound() && range.hasUpperBound()) {
            Comparable<?> lowerEndpoint = parseNumberByClazz(range.lowerEndpoint().toString(), clazz);
            Comparable<?> upperEndpoint = parseNumberByClazz(range.upperEndpoint().toString(), clazz);
            return Range.range(lowerEndpoint, range.lowerBoundType(), upperEndpoint, range.upperBoundType());
        }
        if (!range.hasLowerBound() && !range.hasUpperBound()) {
            return Range.all();
        }
        if (range.hasLowerBound()) {
            Comparable<?> lowerEndpoint = parseNumberByClazz(range.lowerEndpoint().toString(), clazz);
            return Range.downTo(lowerEndpoint, range.lowerBoundType());
        }
        Comparable<?> upperEndpoint = parseNumberByClazz(range.upperEndpoint().toString(), clazz);
        return Range.upTo(upperEndpoint, range.upperBoundType());
    }
    
    private static Optional<Class<?>> getTargetNumericType(final List<Comparable<?>> endpoints) {
        Set<Class<?>> classes = endpoints.stream().filter(Objects::nonNull).map(Comparable::getClass).collect(Collectors.toSet());
        Class<?> clazz = null;
        if (classes.contains(BigDecimal.class)) {
            clazz = BigDecimal.class;
        } else if (classes.contains(Double.class)) {
            clazz = Double.class;
        } else if (classes.contains(Float.class)) {
            clazz = Float.class;
        } else if (classes.contains(BigInteger.class)) {
            clazz = BigInteger.class;
        } else if (classes.contains(Long.class)) {
            clazz = Long.class;
        } else if (classes.contains(Integer.class)) {
            clazz = Integer.class;
        }
        return Optional.ofNullable(clazz);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Comparable<?> parseNumberByClazz(final String number, final Class<?> clazz) {
        return (Comparable<?>) clazz.getConstructor(String.class).newInstance(number);
    }
}
