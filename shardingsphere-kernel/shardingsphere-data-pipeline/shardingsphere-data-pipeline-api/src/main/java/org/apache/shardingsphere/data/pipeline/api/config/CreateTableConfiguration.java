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

package org.apache.shardingsphere.data.pipeline.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;

import java.util.Collection;

/**
 * Create table configuration.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class CreateTableConfiguration {
    
    private final Collection<CreateTableEntry> createTableEntries;
    
    @RequiredArgsConstructor
    @Getter
    @ToString(exclude = {"sourceDataSourceConfig", "targetDataSourceConfig"})
    public static final class CreateTableEntry {
        
        private final PipelineDataSourceConfiguration sourceDataSourceConfig;
        
        private final SchemaTableName sourceName;
        
        private final PipelineDataSourceConfiguration targetDataSourceConfig;
        
        private final SchemaTableName targetName;
    }
}
