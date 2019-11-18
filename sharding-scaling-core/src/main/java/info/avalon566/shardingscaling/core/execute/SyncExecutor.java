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

package info.avalon566.shardingscaling.core.execute;

import info.avalon566.shardingscaling.core.exception.SyncExecuteException;
import info.avalon566.shardingscaling.core.execute.channel.Channel;
import info.avalon566.shardingscaling.core.execute.reader.Reader;
import info.avalon566.shardingscaling.core.execute.writer.Writer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
     * @throws SyncExecuteException execute execute exception
     */
    public void execute() throws SyncExecuteException {
        List<Future<?>> futures = new ArrayList<>(syncRunners.size());
        ExecutorService executorService = Executors.newFixedThreadPool(syncRunners.size());
        CompletionService<?> completionService = new ExecutorCompletionService(executorService);
        for (SyncRunner syncRunner : syncRunners) {
            futures.add(completionService.submit(syncRunner, null));
        }
        executorService.shutdown();
        waitExecute(futures, completionService);
    }

    private void waitExecute(final List<Future<?>> futures, final CompletionService<?> completionService) throws SyncExecuteException {
        int exitedCount = 1;
        while (futures.size() >= exitedCount) {
            try {
                handleResult(completionService.take(), futures);
            } catch (InterruptedException ignored) {
            }
            exitedCount++;
        }
        if (null != syncExecuteException) {
            throw syncExecuteException;
        }
    }

    private void handleResult(final Future<?> future, final List<Future<?>> allFutures) {
        try {
            future.get();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            if (null == syncExecuteException) {
                syncExecuteException = new SyncExecuteException();
            }
            syncExecuteException.addException(e.getCause());
            stopExecute(allFutures);
        }
    }

    private void stopExecute(final List<Future<?>> allFutures) {
        if (!running) {
            return;
        }
        running = false;
        for (SyncRunner syncRunner : syncRunners) {
            syncRunner.stop();
        }
    }
}
