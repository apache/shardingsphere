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

package org.apache.shardingsphere.shardingscaling.core.execute.engine;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncRunner;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.Reader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.Writer;

import java.util.LinkedList;
import java.util.List;

/**
 * Execute util.
 *
 * @author avalon566
 */
@Slf4j
public class ExecuteUtil {

    private static final SyncTaskExecuteEngine EXECUTE_ENGINE = new DefaultSyncTaskExecuteEngine(ScalingContext.getInstance().getServerConfiguration().getWorkerThread());

    /**
     * Execute.
     *
     * @param channel channel
     * @param reader reader
     * @param writers writers
     * @param executeCallback call when execute finish
     */
    public static void execute(final Channel channel, final Reader reader, final List<Writer> writers, final ExecuteCallback executeCallback) {
        final List<SyncRunner> syncRunners = new LinkedList<>();
        reader.setChannel(channel);
        syncRunners.add(reader);
        for (Writer writer : writers) {
            writer.setChannel(channel);
            syncRunners.add(writer);
        }
        Iterable<ListenableFuture<Object>> listenableFutures = EXECUTE_ENGINE.submit(syncRunners);
        ListenableFuture allListenableFuture = Futures.allAsList(listenableFutures);
        Futures.addCallback(allListenableFuture, new FutureCallback<List<Object>>() {

            @Override
            public void onSuccess(final List<Object> result) {
                executeCallback.onSuccess();
            }

            @Override
            public void onFailure(final Throwable t) {
                for (SyncRunner syncRunner : syncRunners) {
                    syncRunner.stop();
                }
                executeCallback.onFailure(t);
            }
        });
    }
}
