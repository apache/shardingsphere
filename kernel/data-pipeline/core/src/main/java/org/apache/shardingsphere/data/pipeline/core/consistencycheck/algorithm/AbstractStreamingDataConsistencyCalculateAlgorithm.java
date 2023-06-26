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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCalculatedResult;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Streaming data consistency calculate algorithm.
 */
@Getter
@Slf4j
public abstract class AbstractStreamingDataConsistencyCalculateAlgorithm extends AbstractDataConsistencyCalculateAlgorithm {
    
    @Override
    public final Iterable<DataConsistencyCalculatedResult> calculate(final DataConsistencyCalculateParameter param) {
        return new ResultIterable(param);
    }
    
    /**
     * Calculate chunked records at one time.
     *
     * @param param data consistency calculate parameter
     * @return optional calculated result, empty means there's no more result
     */
    protected abstract Optional<DataConsistencyCalculatedResult> calculateChunk(DataConsistencyCalculateParameter param);
    
    /**
     * It's not thread-safe, it should be executed in only one thread at the same time.
     */
    @RequiredArgsConstructor
    private final class ResultIterable implements Iterable<DataConsistencyCalculatedResult> {
        
        private final DataConsistencyCalculateParameter param;
        
        @Override
        public Iterator<DataConsistencyCalculatedResult> iterator() {
            return new ResultIterator(param);
        }
    }
    
    @RequiredArgsConstructor
    private final class ResultIterator implements Iterator<DataConsistencyCalculatedResult> {
        
        private final AtomicBoolean currentChunkCalculated = new AtomicBoolean();
        
        private final AtomicReference<Optional<DataConsistencyCalculatedResult>> nextResult = new AtomicReference<>();
        
        private final DataConsistencyCalculateParameter param;
        
        @Override
        public boolean hasNext() {
            calculateIfNecessary();
            return nextResult.get().isPresent();
        }
        
        @Override
        public DataConsistencyCalculatedResult next() {
            calculateIfNecessary();
            Optional<DataConsistencyCalculatedResult> result = nextResult.get();
            nextResult.set(null);
            currentChunkCalculated.set(false);
            return result.orElseThrow(NoSuchElementException::new);
        }
        
        private void calculateIfNecessary() {
            if (!currentChunkCalculated.get()) {
                nextResult.set(calculateChunk(param));
                currentChunkCalculated.set(true);
            }
        }
    }
}
