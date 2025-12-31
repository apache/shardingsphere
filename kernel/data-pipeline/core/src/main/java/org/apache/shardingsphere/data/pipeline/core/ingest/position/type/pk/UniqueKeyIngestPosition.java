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

package org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;

import java.math.BigInteger;
import java.util.List;

/**
 * Unique key ingest position.
 *
 * @param <T> type of value
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniqueKeyIngestPosition<T> implements IngestPosition {
    
    @Getter
    private final char type;
    
    private final Range<T> range;
    
    /**
     * New instance by lower bound and upper bound.
     *
     * @param <T> type of value
     * @param range range
     * @return ingest position
     */
    public static <T> UniqueKeyIngestPosition<?> newInstance(final Range<T> range) {
        T lowerBound = range.getLowerBound();
        T upperBound = range.getUpperBound();
        if (lowerBound instanceof Number) {
            BigInteger lower = convertToBigInteger((Number) lowerBound);
            BigInteger upper = null == upperBound ? null : convertToBigInteger((Number) upperBound);
            return ofInteger(range.isLowerInclusive() ? Range.closed(lower, upper) : Range.openClosed(lower, upper));
        }
        if (lowerBound instanceof CharSequence) {
            String lower = lowerBound.toString();
            String upper = null == upperBound ? null : upperBound.toString();
            return ofString(range.isLowerInclusive() ? Range.closed(lower, upper) : Range.openClosed(lower, upper));
        }
        // TODO support more types, e.g. byte[] (MySQL varbinary)
        return ofUnsplit();
    }
    
    private static BigInteger convertToBigInteger(final Number number) {
        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }
        return BigInteger.valueOf(number.longValue());
    }
    
    /**
     * Create integer unique key ingest position.
     *
     * @param range range
     * @return integer unique key ingest position
     */
    public static UniqueKeyIngestPosition<BigInteger> ofInteger(final Range<BigInteger> range) {
        return new UniqueKeyIngestPosition<>('i', range);
    }
    
    /**
     * Create string unique key ingest position.
     *
     * @param range range
     * @return string unique key ingest position
     */
    public static UniqueKeyIngestPosition<String> ofString(final Range<String> range) {
        return new UniqueKeyIngestPosition<>('s', range);
    }
    
    /**
     * Create unsplit unique key ingest position.
     *
     * @return unsplit unique key ingest position
     */
    public static UniqueKeyIngestPosition<Void> ofUnsplit() {
        return new UniqueKeyIngestPosition<>('u', Range.closed(null, null));
    }
    
    /**
     * Create new instance by text.
     *
     * @param text text
     * @return unique key position
     * @throws IllegalArgumentException illegal argument exception
     */
    public static UniqueKeyIngestPosition<?> decode(final String text) {
        List<String> parts = Splitter.on(',').splitToList(text);
        Preconditions.checkArgument(3 == parts.size(), "Unknown unique key position: " + text);
        Preconditions.checkArgument(1 == parts.get(0).length(), "Invalid unique key position type: " + parts.get(0));
        char type = parts.get(0).charAt(0);
        String lowerBound = parts.get(1);
        String upperBound = parts.get(2);
        switch (type) {
            case 'i':
                BigInteger lower = Strings.isNullOrEmpty(lowerBound) ? null : new BigInteger(lowerBound);
                BigInteger upper = Strings.isNullOrEmpty(upperBound) ? null : new BigInteger(upperBound);
                return ofInteger(Range.closed(lower, upper));
            case 's':
                return ofString(Range.closed(lowerBound, upperBound));
            case 'u':
                return ofUnsplit();
            default:
                throw new IllegalArgumentException("Unknown unique key position type: " + type);
        }
    }
    
    /**
     * Encode to text.
     *
     * @return encoded text
     * @throws RuntimeException runtime exception
     */
    public String encode() {
        T lowerBound = getLowerBound();
        T upperBound = getUpperBound();
        String encodedLowerBound;
        String encodedUpperBound;
        switch (getType()) {
            case 'i':
            case 's':
            case 'u':
                encodedLowerBound = null == lowerBound ? "" : lowerBound.toString();
                encodedUpperBound = null == upperBound ? "" : upperBound.toString();
                break;
            default:
                throw new RuntimeException("Unknown unique key position type: " + getType());
        }
        return String.format("%s,%s,%s", getType(), encodedLowerBound, encodedUpperBound);
    }
    
    /**
     * Get lower bound.
     *
     * @return lower bound
     */
    public T getLowerBound() {
        return range.getLowerBound();
    }
    
    /**
     * Get upper bound.
     *
     * @return upper bound
     */
    public T getUpperBound() {
        return range.getUpperBound();
    }
    
    @Override
    public String toString() {
        // TODO Add encode() method in IngestPosition interface, and remove .toString() invocations.
        return encode();
    }
}
