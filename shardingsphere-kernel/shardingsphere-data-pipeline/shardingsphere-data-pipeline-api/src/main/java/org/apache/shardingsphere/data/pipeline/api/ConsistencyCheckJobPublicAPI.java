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

package org.apache.shardingsphere.data.pipeline.api;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;

import java.util.Map;

/**
 * Migration job public API.
 */
@SingletonSPI
public interface ConsistencyCheckJobPublicAPI extends InventoryIncrementalJobPublicAPI, RequiredSPI {
    
    /**
     * Create job migration config and start.
     *
     * @param parameter create consistency check job parameter
     * @return job id
     */
    String createJobAndStart(CreateConsistencyCheckJobParameter parameter);
    
    /**
     * Get latest data consistency check result.
     *
     * @param jobId job id
     * @return latest data consistency check result
     */
    Map<String, DataConsistencyCheckResult> getLatestDataConsistencyCheckResult(String jobId);
}
