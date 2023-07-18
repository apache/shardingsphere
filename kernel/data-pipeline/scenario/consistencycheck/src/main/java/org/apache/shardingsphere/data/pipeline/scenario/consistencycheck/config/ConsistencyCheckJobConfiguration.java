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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;

import java.util.Properties;

/**
 * Consistency check job configuration.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ConsistencyCheckJobConfiguration implements PipelineJobConfiguration {
    
    private final String jobId;
    
    private final String parentJobId;
    
    private final String algorithmTypeName;
    
    private final Properties algorithmProps;
    
    @Override
    public DatabaseType getSourceDatabaseType() {
        throw new UnsupportedOperationException("");
    }
    
    /**
     * Get job sharding count.
     *
     * @return job sharding count
     */
    @Override
    public int getJobShardingCount() {
        return 1;
    }
}
