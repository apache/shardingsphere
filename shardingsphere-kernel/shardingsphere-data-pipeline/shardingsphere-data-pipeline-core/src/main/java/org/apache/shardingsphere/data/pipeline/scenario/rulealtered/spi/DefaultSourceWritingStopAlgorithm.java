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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered.spi;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.data.pipeline.spi.lock.RowBasedJobLockAlgorithm;

/**
 * Default source writing stop algorithm.
 */
@Slf4j
public final class DefaultSourceWritingStopAlgorithm implements RowBasedJobLockAlgorithm {
    
    private final RuleAlteredJobAPI ruleAlteredJobAPI = PipelineJobAPIFactory.newInstance();
    
    @Override
    public void lock(final String schemaName, final String jobId) {
        log.info("lock, schemaName={}, jobId={}", schemaName, jobId);
        ruleAlteredJobAPI.stopClusterWriteDB(schemaName, jobId);
    }
    
    @Override
    public void releaseLock(final String schemaName, final String jobId) {
        log.info("releaseLock, schemaName={}, jobId={}", schemaName, jobId);
        ruleAlteredJobAPI.restoreClusterWriteDB(schemaName, jobId);
    }
}
