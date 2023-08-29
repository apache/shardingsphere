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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract streaming single table inventory calculator.
 */
@Getter
@Slf4j
public abstract class AbstractStreamingSingleTableInventoryCalculator extends AbstractSingleTableInventoryCalculator {
    
    @Override
    public final Iterable<SingleTableInventoryCalculatedResult> calculate(final SingleTableInventoryCalculateParameter param) {
        return new ResultIterable(param);
    }
    
    /**
     * Calculate chunked records at one time.
     *
     * @param param data consistency calculate parameter
     * @return optional calculated result, empty means there's no more result
     */
    protected abstract Optional<SingleTableInventoryCalculatedResult> calculateChunk(SingleTableInventoryCalculateParameter param);
    
    /**
     * It's not thread-safe, it should be executed in only one thread at the same time.
     */
    @RequiredArgsConstructor
    private final class ResultIterable implements Iterable<SingleTableInventoryCalculatedResult> {
        
        private final SingleTableInventoryCalculateParameter param;
        
        @Override
        public Iterator<SingleTableInventoryCalculatedResult> iterator() {
            return new ResultIterator(param);
        }
    }
    
    @RequiredArgsConstructor
    private final class ResultIterator implements Iterator<SingleTableInventoryCalculatedResult> {
        
        private final AtomicBoolean currentChunkCalculated = new AtomicBoolean();
        
        private final AtomicReference<Optional<SingleTableInventoryCalculatedResult>> nextResult = new AtomicReference<>();
        
        private final SingleTableInventoryCalculateParameter param;
        
        @Override
        public boolean hasNext() {
            calculateIfNecessary();
            return nextResult.get().isPresent();
        }
        
        @Override
        public SingleTableInventoryCalculatedResult next() {
            calculateIfNecessary();
            Optional<SingleTableInventoryCalculatedResult> result = nextResult.get();
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
