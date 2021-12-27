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

package org.apache.shardingsphere.data.pipeline.api.check.consistency;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;

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
     * Data source of source side or target side.
     * Do not close it, it will be reused later.
     */
    private PipelineDataSourceWrapper dataSource;
    
    private String logicTableName;
    
    /**
     * All column names of logic table.
     */
    private Collection<String> columnNames;
    
    /**
     * Database type.
     */
    private String databaseType;
    
    /**
     * Peer database type.
     */
    private String peerDatabaseType;
    
    /**
     * It could be primary key.
     * It could be used in order by clause.
     */
    private String uniqueKey;
    
    /**
     * Used for range query.
     * If it's configured, then it could be translated to SQL like "uniqueColumn >= pair.left AND uniqueColumn <= pair.right".
     * One of left and right of pair could be null.
     */
    private Pair<Object, Object> uniqueColumnValueRange;
    
    /**
     * Used for multiple records query.
     * If it's configured, then it could be translated to SQL like "uniqueColumn IN (value1,value2,value3)".
     */
    private Collection<Object> uniqueColumnValues;
    
    /**
     * Previous calculated result will be transferred to next call.
     */
    private volatile Object previousCalculatedResult;
}
