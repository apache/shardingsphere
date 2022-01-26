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

package org.apache.shardingsphere.data.pipeline.core.datasource.creator.impl;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreator;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;

import javax.sql.DataSource;

/**
 * Standard pipeline data source creator.
 */
public final class StandardPipelineDataSourceCreator implements PipelineDataSourceCreator {
    
    @Override
    public DataSource createPipelineDataSource(final Object pipelineDataSourceConfig) {
        return DataSourcePoolCreator.create((DataSourceProperties) pipelineDataSourceConfig);
    }
    
    @Override
    public String getType() {
        return StandardPipelineDataSourceConfiguration.TYPE;
    }
}
