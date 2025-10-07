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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.util.List;

/**
 * Table inventory check parameter.
 */
@RequiredArgsConstructor
@Getter
public final class TableInventoryCheckParameter {
    
    private final String jobId;
    
    private final int splittingItem;
    
    private final PipelineDataSource sourceDataSource;
    
    private final PipelineDataSource targetDataSource;
    
    private final QualifiedTable sourceTable;
    
    private final QualifiedTable targetTable;
    
    private final List<String> columnNames;
    
    private final List<PipelineColumnMetaData> uniqueKeys;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final ConsistencyCheckJobItemProgressContext progressContext;
    
    private final String queryCondition;
    
    public TableInventoryCheckParameter(final String jobId, final PipelineDataSource sourceDataSource, final PipelineDataSource targetDataSource,
                                        final QualifiedTable sourceTable, final QualifiedTable targetTable,
                                        final List<String> columnNames, final List<PipelineColumnMetaData> uniqueKeys,
                                        final JobRateLimitAlgorithm readRateLimitAlgorithm, final ConsistencyCheckJobItemProgressContext progressContext) {
        this(jobId, 0, sourceDataSource, targetDataSource, sourceTable, targetTable, columnNames, uniqueKeys, readRateLimitAlgorithm, progressContext,
                null);
    }
}
