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

package org.apache.shardingsphere.data.pipeline.core.metadata;

import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;

/**
 * Pipeline meta data persist service.
 *
 * @param <T> type of configuration
 */
public interface PipelineMetaDataPersistService<T> {
    
    /**
     * Load meta data.
     *
     * @param contextKey context key
     * @param jobType job type, nullable
     * @return configurations
     */
    T load(PipelineContextKey contextKey, JobType jobType);
    
    /**
     * Persist meta data.
     *
     * @param contextKey context key
     * @param jobType job type, nullable
     * @param configs configurations
     */
    void persist(PipelineContextKey contextKey, JobType jobType, T configs);
}
