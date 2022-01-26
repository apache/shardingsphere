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

package org.apache.shardingsphere.data.pipeline.api.ingest.channel;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;

import java.util.List;

/**
 * Pipeline channel.
 */
public interface PipelineChannel {
    
    /**
     * Push {@code DataRecord} into channel.
     *
     * @param dataRecord data
     */
    void pushRecord(Record dataRecord);
    
    /**
     * Fetch {@code Record} list from channel.
     * It might be blocked at most timeout seconds if available records count doesn't reach batch size.
     *
     * @param batchSize record batch size
     * @param timeoutSeconds timeout(seconds)
     * @return record
     */
    List<Record> fetchRecords(int batchSize, int timeoutSeconds);
    
    /**
     * Ack the last batch.
     *
     * @param records record list
     */
    void ack(List<Record> records);
    
    /**
     * Close channel.
     */
    void close();
}
