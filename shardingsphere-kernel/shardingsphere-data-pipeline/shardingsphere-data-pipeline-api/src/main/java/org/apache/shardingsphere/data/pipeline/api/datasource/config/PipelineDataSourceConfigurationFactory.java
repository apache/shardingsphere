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

package org.apache.shardingsphere.data.pipeline.api.datasource.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.util.exception.external.sql.UnsupportedSQLOperationException;

/**
 * Pipeline data source configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineDataSourceConfigurationFactory {
    
    /**
     * Create new instance of pipeline data source configuration.
     *
     * @param type type of pipeline data source configuration
     * @param parameter parameter of pipeline data source configuration
     * @return created instance
     */
    public static PipelineDataSourceConfiguration newInstance(final String type, final String parameter) {
        switch (type) {
            case StandardPipelineDataSourceConfiguration.TYPE:
                return new StandardPipelineDataSourceConfiguration(parameter);
            case ShardingSpherePipelineDataSourceConfiguration.TYPE:
                return new ShardingSpherePipelineDataSourceConfiguration(parameter);
            default:
                throw new UnsupportedSQLOperationException(String.format("Unsupported data source type `%s`", type));
        }
    }
}
