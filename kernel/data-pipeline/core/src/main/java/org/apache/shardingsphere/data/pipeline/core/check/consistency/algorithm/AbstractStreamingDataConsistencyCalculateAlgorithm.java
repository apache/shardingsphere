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

package org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Streaming data consistency calculate algorithm.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class AbstractStreamingDataConsistencyCalculateAlgorithm extends AbstractDataConsistencyCalculateAlgorithm {
    
    @Override
    public final Iterable<DataConsistencyCalculatedResult> calculate(final DataConsistencyCalculateParameter parameter) {
        return new ResultIterable(parameter);
    }
    
    /**
     * Calculate chunked records at one time.
     *
     * @param parameter data consistency calculate parameter
     * @return optional calculated result, empty means there's no more result
     */
    protected abstract Optional<DataConsistencyCalculatedResult> calculateChunk(DataConsistencyCalculateParameter parameter);
    
    /**
     * It's not thread-safe, it should be executed in only one thread at the same time.
     */
    @RequiredArgsConstructor
    final class ResultIterable implements Iterable<DataConsistencyCalculatedResult> {
        
        private final DataConsistencyCalculateParameter parameter;
        
        @Override
        public Iterator<DataConsistencyCalculatedResult> iterator() {
            return new ResultIterator(parameter);
        }
    }
    
    @RequiredArgsConstructor
    final class ResultIterator implements Iterator<DataConsistencyCalculatedResult> {
        
        private final DataConsistencyCalculateParameter parameter;
        
        private final AtomicInteger calculationCount = new AtomicInteger(0);
        
        private volatile Optional<DataConsistencyCalculatedResult> nextResult;
        
        @Override
        public boolean hasNext() {
            calculateIfNecessary();
            return nextResult.isPresent();
        }
        
        @Override
        public DataConsistencyCalculatedResult next() {
            calculateIfNecessary();
            Optional<DataConsistencyCalculatedResult> nextResult = this.nextResult;
            parameter.setPreviousCalculatedResult(nextResult.orElse(null));
            this.nextResult = null;
            return nextResult.orElse(null);
        }
        
        private void calculateIfNecessary() {
            if (null != nextResult) {
                return;
            }
            nextResult = calculateChunk(parameter);
            if (!nextResult.isPresent()) {
                log.info("nextResult not present, calculation done. calculationCount={}", calculationCount);
            }
            if (0 == calculationCount.incrementAndGet() % 100_0000) {
                log.warn("possible infinite loop, calculationCount={}", calculationCount);
            }
        }
    }
}
