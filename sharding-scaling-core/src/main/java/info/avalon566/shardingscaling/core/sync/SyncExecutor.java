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

package info.avalon566.shardingscaling.core.sync;

import info.avalon566.shardingscaling.core.exception.SyncExecuteException;
import info.avalon566.shardingscaling.core.sync.channel.Channel;
import info.avalon566.shardingscaling.core.sync.channel.DispatcherChannel;
import info.avalon566.shardingscaling.core.sync.channel.MemoryChannel;
import info.avalon566.shardingscaling.core.sync.reader.Reader;
import info.avalon566.shardingscaling.core.sync.writer.Writer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Sync executor.
 * @author avalon566
 */
@AllArgsConstructor
@Slf4j
public class SyncExecutor {

    private Future<?> readerFuture;

    private final List<Future<?>> writerFutures;

    private final Reader reader;

    private final List<Writer> writers;

    private final Channel channel;

    public SyncExecutor(final Reader reader, final List<Writer> writers) {
        this.reader = reader;
        this.writers = writers;
        this.channel = 1 == writers.size()
                ? new MemoryChannel()
                : new DispatcherChannel(writers.size());
        writerFutures = new ArrayList<>(writers.size());
    }

    /**
     * Run.
     */
    public void run() {
        ExecutorService readThreadExecutor = Executors.newSingleThreadExecutor();
        reader.setChannel(channel);
        readerFuture = readThreadExecutor.submit(reader);
        readThreadExecutor.shutdown();
        ExecutorService writeThreadExecutor = Executors.newFixedThreadPool(writers.size());
        for (Writer writer : writers) {
            writer.setChannel(channel);
            writerFutures.add(writeThreadExecutor.submit(writer));
        }
        writeThreadExecutor.shutdown();
    }

    /**
     * Wait all slices finished.
     */
    public void waitFinish() {
        try {
            readerFuture.get();
            for (Future<?> writerFuture : writerFutures) {
                writerFuture.get();
            }
        } catch (InterruptedException ignored) {
            shutDownReaderAndWriters();
        } catch (ExecutionException ex) {
            shutDownReaderAndWriters();
            handleExecutionException(ex.getCause());
        }
    }
    
    private void shutDownReaderAndWriters() {
        reader.stop();
        for (Writer each : writers) {
            each.stop();
        }
    }
    
    private void handleExecutionException(final Throwable cause) {
        if (cause instanceof SyncExecuteException) {
            throw (SyncExecuteException) cause;
        }
        throw new SyncExecuteException(cause);
    }
    
}
