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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Integer range splitting iterator.
 *
 * <p>It's not thread-safe.</p>
 */
public final class IntegerRangeSplittingIterator implements Iterator<Range<BigInteger>> {
    
    private final BigInteger upperBound;
    
    private final BigInteger stepSize;
    
    private BigInteger current;
    
    public IntegerRangeSplittingIterator(final BigInteger lowerBound, final BigInteger upperBound, final BigInteger stepSize) {
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lower bounder greater than upper bound");
        }
        if (stepSize.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("step size is less than zero");
        }
        this.upperBound = upperBound;
        this.stepSize = stepSize;
        current = lowerBound;
    }
    
    @Override
    public boolean hasNext() {
        return current.compareTo(upperBound) <= 0;
    }
    
    @Override
    public Range<BigInteger> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("");
        }
        BigInteger upperLimit = min(upperBound, current.add(stepSize));
        Range<BigInteger> result = Range.closed(current, upperLimit);
        current = upperLimit.add(BigInteger.ONE);
        return result;
    }
    
    private BigInteger min(final BigInteger one, final BigInteger another) {
        return one.compareTo(another) < 0 ? one : another;
    }
}
