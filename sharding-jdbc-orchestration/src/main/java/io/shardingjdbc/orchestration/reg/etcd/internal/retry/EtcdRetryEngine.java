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
