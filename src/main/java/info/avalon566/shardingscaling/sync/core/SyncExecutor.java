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

package info.avalon566.shardingscaling.sync.core;

import lombok.AllArgsConstructor;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author avalon566
 */
@AllArgsConstructor
public class SyncExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncExecutor.class);

    private Future<?> readerFuture;
    private final List<Future<?>> writerFutures;
    private final Reader reader;
    private final List<Writer> writers;
    private final Channel channel;

    public SyncExecutor(Reader reader, List<Writer> writers) {
        this.reader = reader;
        this.writers = writers;
        this.channel = 1 == writers.size() ?
                new MemoryChannel() :
                new DispatcherChannel(writers.size());
        writerFutures = new ArrayList<>(writers.size());
    }

    public void run() {
        var pool1 = Executors.newSingleThreadExecutor();
        reader.setChannel(channel);
        readerFuture = pool1.submit(reader);
        pool1.shutdown();
        var pool2 = Executors.newFixedThreadPool(writers.size());
        for (Writer writer : writers) {
            writer.setChannel(channel);
            writerFutures.add(pool2.submit(writer));
        }
        pool2.shutdown();
    }

    public void waitFinish() {
        for (Future<?> writerFuture : writerFutures) {
            try {
                writerFuture.get();
            } catch (Exception ex) {
                //TODO: shutdown reader and other writer
                throw new RuntimeException(ex);
            }
        }
    }
}
