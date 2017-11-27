/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.reg.etcd.internal.retry;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Optional;
import io.shardingjdbc.orchestration.reg.etcd.EtcdConfiguration;
import io.shardingjdbc.orchestration.reg.exception.RegExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Etcd retry engine.
 *
 * @author junxiong
 */
@RequiredArgsConstructor
public final class EtcdRetryEngine {
    
    private final EtcdConfiguration etcdConfig;
    
    /**
     * Retry to execute callable command.
     * 
     * @param callable callable command
     * @param <T> return type
     * @return execute result
     */
    public <T> Optional<T> execute(final Callable<T> callable) {
        Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
                .retryIfExceptionOfType(TimeoutException.class)
                .retryIfExceptionOfType(ExecutionException.class)
                .retryIfExceptionOfType(InterruptedException.class)
                .withWaitStrategy(WaitStrategies.fixedWait(etcdConfig.getRetryIntervalMilliseconds(), TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(etcdConfig.getMaxRetries()))
                .build();
        try {
            return Optional.fromNullable(retryer.call(callable));
        } catch (final ExecutionException | RetryException ex) {
            RegExceptionHandler.handleException(ex);
            return Optional.absent();
        }
    }
}
