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

package org.apache.shardingsphere.scaling.core.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.infra.config.datasource.typed.TypedDataSourceConfiguration;

import java.util.Collection;

/**
 * Data calculate parameter.
 */
@Getter
@Setter
@Builder
@ToString
public final class DataCalculateParameter {
    
    /**
     * Data source configuration of source side or target side.
     */
    private TypedDataSourceConfiguration dataSourceConfig;
    
    private String logicTableName;
    
    /**
     * All column names of logic table.
     */
    private Collection<String> columnNames;
    
    /**
     * Peer database type.
     */
    private String peerDatabaseType;
    
    /**
     * Chunk size of limited records to be calculated in a batch.
     */
    private Integer chunkSize;
    
    /**
     * Ignored column names.
     */
    private Collection<String> ignoredColumnNames;
    
    /**
     * If {@link #chunkSize} exists, it could be used in order by clause on first priority.
     */
    private Collection<String> primaryColumnNames;
    
    /**
     * If {@link #chunkSize} exists, it could be used in order by clause on second priority.
     */
    private Collection<String> uniqueColumnNames;
}
