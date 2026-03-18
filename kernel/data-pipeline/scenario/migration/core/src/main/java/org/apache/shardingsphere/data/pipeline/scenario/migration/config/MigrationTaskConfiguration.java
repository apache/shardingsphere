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

package org.apache.shardingsphere.data.pipeline.scenario.migration.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.task.config.PipelineTaskConfiguration;

import java.util.Collection;

/**
 * Migration task configuration.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class MigrationTaskConfiguration implements PipelineTaskConfiguration {
    
    private final String dataSourceName;
    
    private final Collection<CreateTableConfiguration> createTableConfigurations;
    
    private final IncrementalDumperContext dumperContext;
    
    private final ImporterConfiguration importerConfig;
}
