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

package info.avalon566.shardingscaling.core.execute.engine;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import info.avalon566.shardingscaling.core.exception.SyncExecuteException;
import info.avalon566.shardingscaling.core.execute.executor.SyncRunner;
import info.avalon566.shardingscaling.core.execute.executor.channel.Channel;
import info.avalon566.shardingscaling.core.execute.executor.reader.Reader;
import info.avalon566.shardingscaling.core.execute.executor.writer.Writer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * Sync executor.
 *
 * @author avalon566
 */
@Slf4j
public class SyncExecutor {

    private boolean running = true;

    private final Channel channel;

    private final List<SyncRunner> syncRunners;

    private SyncExecuteException syncExecuteException;

    public SyncExecutor(final Channel channel, final Reader reader, final List<Writer> writers) {
        this.channel = channel;
        int runnerNumber = 1 + writers.size();
        this.syncRunners = new ArrayList<>(runnerNumber);
        reader.setChannel(channel);
        syncRunners.add(reader);
        for (Writer writer : writers) {
            writer.setChannel(channel);
            syncRunners.add(writer);
        }
    }

    /**
     * Execute.
     *
     * @param executeCallback call when execute finish
     */
    public void execute(final ExecuteCallback executeCallback) {
        final CountDownLatch countDownLatch = new CountDownLatch(syncRunners.size());
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(syncRunners.size()));
        for (SyncRunner syncRunner : syncRunners) {
            ListenableFuture listenableFuture = executorService.submit(syncRunner, null);
            Futures.addCallback(listenableFuture, new FutureCallback() {

                @Override
                public void onSuccess(final Object result) {
                    checkFinish();
                }

                @Override
                public void onFailure(final Throwable t) {
                    if (null == syncExecuteException) {
                        syncExecuteException = new SyncExecuteException();
                    }
                    syncExecuteException.addException(t);
                    stopExecute();
                    checkFinish();
                }

                private void checkFinish() {
                    synchronized (this) {
                        countDownLatch.countDown();
                        if (0 == countDownLatch.getCount()) {
                            if (null == syncExecuteException) {
                                executeCallback.onSuccess();
                            } else {
                                executeCallback.onFailure(syncExecuteException);
                            }
                        }
                    }
                }
            });
        }
        executorService.shutdown();
    }

    private void stopExecute() {
        if (!running) {
            return;
        }
        running = false;
        for (SyncRunner syncRunner : syncRunners) {
            syncRunner.stop();
        }
    }
}
