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

package org.apache.shardingsphere.data.pipeline.core.channel;

import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;

import java.util.List;

/**
 * Pipeline channel.
 * <p>It supports multiple push threads and one fetch thread.</p>
 */
public interface PipelineChannel {
    
    /**
     * Push {@code DataRecord} into channel.
     *
     * @param records data records
     */
    void push(List<Record> records);
    
    /**
     * Fetch {@code Record} list from channel.
     * It might be blocked at most timeout seconds if available records count doesn't reach batch size.
     *
     * @param batchSize record batch size
     * @param timeoutMillis timeout millis
     * @return records of transactions
     */
    List<Record> fetch(int batchSize, long timeoutMillis);
    
    /**
     * Peek {@code Record} list from channel.
     *
     * @return records of a transaction
     */
    List<Record> peek();
    
    /**
     * Poll {@code Record} list from channel.
     *
     * @return records of a transaction
     */
    List<Record> poll();
    
    /**
     * Ack the last batch.
     *
     * @param records record list
     */
    // TODO Refactor ack param
    void ack(List<Record> records);
}
