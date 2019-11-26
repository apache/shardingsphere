package org.apache.shardingsphere.shardingscaling.core.execute.engine;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncRunner;

import java.util.List;

/**
 * Sync task execute engine.
 *
 * @author avalon566
 */
public interface SyncTaskExecuteEngine {

    /**
     * Submit sync runner to execute.
     *
     * @param syncRunners sync runner list
     * @return listenable future
     */
    List<ListenableFuture> submit(List<SyncRunner> syncRunners);
}
