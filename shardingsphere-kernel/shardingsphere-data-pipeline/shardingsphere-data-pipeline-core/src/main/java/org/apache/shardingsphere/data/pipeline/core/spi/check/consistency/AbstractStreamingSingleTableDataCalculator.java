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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract single table data calculator.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class AbstractStreamingSingleTableDataCalculator extends AbstractSingleTableDataCalculator {
    
    @Override
    public final Iterable<Object> calculate(final DataCalculateParameter dataCalculateParameter) {
        return new ResultIterable(dataCalculateParameter);
    }
    
    /**
     * Calculate chunked records at one time.
     *
     * @param dataCalculateParameter data calculate parameter
     * @return optional calculated result, empty means there's no more result
     */
    protected abstract Optional<Object> calculateChunk(DataCalculateParameter dataCalculateParameter);
    
    /**
     * It's not thread-safe, it should be executed in only one thread at the same time.
     */
    @RequiredArgsConstructor
    final class ResultIterable implements Iterable<Object> {
        
        private final DataCalculateParameter dataCalculateParameter;
        
        @Override
        public Iterator<Object> iterator() {
            return new ResultIterator(dataCalculateParameter);
        }
    }
    
    @RequiredArgsConstructor
    final class ResultIterator implements Iterator<Object> {
        
        private final DataCalculateParameter dataCalculateParameter;
        
        private final AtomicInteger calculationCount = new AtomicInteger(0);
        
        private volatile Optional<Object> nextResult;
        
        @Override
        public boolean hasNext() {
            calculateIfNecessary();
            return nextResult.isPresent();
        }
        
        @Override
        public Object next() {
            calculateIfNecessary();
            Optional<Object> nextResult = this.nextResult;
            dataCalculateParameter.setPreviousCalculatedResult(nextResult.orElse(null));
            this.nextResult = null;
            return nextResult;
        }
        
        private void calculateIfNecessary() {
            if (null != nextResult) {
                return;
            }
            nextResult = calculateChunk(dataCalculateParameter);
            if (!nextResult.isPresent()) {
                log.info("nextResult not present, calculation done. calculationCount={}", calculationCount);
            }
            if (calculationCount.incrementAndGet() % 100_0000 == 0) {
                log.warn("possible infinite loop, calculationCount={}", calculationCount);
            }
        }
    }
}
