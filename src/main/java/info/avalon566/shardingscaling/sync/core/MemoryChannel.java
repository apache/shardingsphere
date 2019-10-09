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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Memory channel.
 * @author avalon566
 */
public class MemoryChannel implements Channel {

    public static final int PUSH_TIMEOUT = 1000;

    private final BlockingQueue<Record> queue = new ArrayBlockingQueue<>(1000);

    @Override
    public final void pushRecord(final Record dataRecord) {
        try {
            if (!queue.offer(dataRecord, PUSH_TIMEOUT, TimeUnit.HOURS)) {
                throw new RuntimeException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final Record popRecord() {
        try {
            return queue.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }
}
