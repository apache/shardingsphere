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

package org.apache.shardingsphere.data.pipeline.core.importer.sink;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;

import java.io.Closeable;
import java.util.List;

/**
 * Pipeline sink.
 */
public interface PipelineSink extends Closeable {
    
    /**
     * Identifier matched or not.
     *
     * @param identifier sink identifier
     * @return true if matched, otherwise false
     */
    boolean identifierMatched(Object identifier);
    
    /**
     * Write data.
     *
     * @param ackId ack id
     * @param records records
     * @return job progress updated parameter
     */
    PipelineJobProgressUpdatedParameter write(String ackId, List<Record> records);
}
