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

package org.apache.shardingsphere.data.pipeline.core.metadata.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pipeline schema util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineSchemaUtil {
    
    /**
     * Get default schema by connection.getSchema().
     *
     * @param dataSourceConfig pipeline data source configuration
     * @return schema
     */
    @SneakyThrows(SQLException.class)
    public static String getDefaultSchema(final PipelineDataSourceConfiguration dataSourceConfig) {
        try (PipelineDataSourceWrapper dataSource = PipelineDataSourceFactory.newInstance(dataSourceConfig)) {
            try (Connection connection = dataSource.getConnection()) {
                String result = connection.getSchema();
                log.info("get default schema {}", result);
                return result;
            }
        }
    }
}
