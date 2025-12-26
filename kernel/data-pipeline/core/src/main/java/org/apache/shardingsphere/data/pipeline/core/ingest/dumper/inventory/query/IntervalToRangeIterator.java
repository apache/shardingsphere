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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query;

import org.apache.commons.lang3.Range;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Interval to range iterator.
 * <p>
 * It's not thread-safe.
 * </p>
 */
public final class IntervalToRangeIterator implements Iterator<Range<Long>> {
    
    private final BigInteger maximum;
    
    private final BigInteger interval;
    
    private BigInteger current;
    
    public IntervalToRangeIterator(final long minimum, final long maximum, final long interval) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum greater than maximum");
        }
        if (interval < 0L) {
            throw new IllegalArgumentException("interval is less than zero");
        }
        this.maximum = BigInteger.valueOf(maximum);
        this.interval = BigInteger.valueOf(interval);
        current = BigInteger.valueOf(minimum);
    }
    
    @Override
    public boolean hasNext() {
        return current.compareTo(maximum) <= 0;
    }
    
    @Override
    public Range<Long> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("");
        }
        BigInteger upperLimit = min(maximum, current.add(interval));
        Range<Long> result = Range.of(current.longValue(), upperLimit.longValue());
        current = upperLimit.add(BigInteger.ONE);
        return result;
    }
    
    private BigInteger min(final BigInteger integer1, final BigInteger integer2) {
        return integer1.compareTo(integer2) < 0 ? integer1 : integer2;
    }
}
