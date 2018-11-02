/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.keygen;

import io.shardingsphere.core.keygen.fixture.FixedTimeService;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DefaultKeyGeneratorTest {
    
    @Test
    public void assertGenerateKey() throws ExecutionException, InterruptedException {
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        final int taskNumber = threadNumber << 2;
        final DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        Set<Number> generatedKeys = new HashSet<>();
        for (int i = 0; i < taskNumber; i++) {
            generatedKeys.add(executor.submit(new Callable<Number>() {
                
                @Override
                public Number call() {
                    return keyGenerator.generateKey();
                }
            }).get());
        }
        assertThat(generatedKeys.size(), is(taskNumber));
    }
    
    @Test
    public void assertMaxSequence() {
        assertThat(maxId((1 << 12) - 1), is((1L << 12L) - 2));
        assertThat(maxId(1 << 12), is((1L << 12L) - 1));
        assertThat(maxId((1 << 12) + 1), is(1L << 22));
    }
    
    private long maxId(final int maxSequence) {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        DefaultKeyGenerator.setTimeService(new FixedTimeService(1 << 13));
        long result = 0;
        for (int i = 0; i < maxSequence; i++) {
            result = keyGenerator.generateKey().longValue();
        }
        return result;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenNegative() {
        DefaultKeyGenerator.setWorkerId(-1L);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetWorkerIdFailureWhenTooMuch() {
        DefaultKeyGenerator.setWorkerId(-Long.MAX_VALUE);
    }
    
    @Test
    @SneakyThrows
    public void assertSetWorkerIdSuccess() {
        DefaultKeyGenerator.setWorkerId(1L);
        Field workerIdField = DefaultKeyGenerator.class.getDeclaredField("workerId");
        workerIdField.setAccessible(true);
        assertThat(workerIdField.getLong(DefaultKeyGenerator.class), is(1L));
        DefaultKeyGenerator.setWorkerId(0L);
    }
}
