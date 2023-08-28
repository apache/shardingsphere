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

package org.apache.shardingsphere.data.pipeline.common.datasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.datasource.creator.PipelineDataSourceCreator;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Pipeline data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineDataSourceFactory {
    
    /**
     * New instance data source wrapper.
     *
     * @param pipelineDataSourceConfig pipeline data source configuration
     * @return new data source wrapper
     */
    @SneakyThrows(SQLException.class)
    public static PipelineDataSourceWrapper newInstance(final PipelineDataSourceConfiguration pipelineDataSourceConfig) {
        DataSource dataSource = TypedSPILoader.getService(
                PipelineDataSourceCreator.class, pipelineDataSourceConfig.getType()).createPipelineDataSource(pipelineDataSourceConfig.getDataSourceConfiguration());
        return new PipelineDataSourceWrapper(dataSource, pipelineDataSourceConfig.getDatabaseType());
    }
}
