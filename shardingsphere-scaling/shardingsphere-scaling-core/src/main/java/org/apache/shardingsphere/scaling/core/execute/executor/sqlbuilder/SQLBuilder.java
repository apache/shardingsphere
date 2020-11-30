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

package org.apache.shardingsphere.scaling.core.execute.executor.sqlbuilder;

import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;

import java.util.Collection;

/**
 * SQL builder.
 */
public interface SQLBuilder {
    
    /**
     * Build insert SQL.
     *
     * @param dataRecord data record
     * @return insert SQL
     */
    String buildInsertSQL(DataRecord dataRecord);
    
    /**
     * Build update SQL.
     *
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return update SQL
     */
    String buildUpdateSQL(DataRecord dataRecord, Collection<Column> conditionColumns);
    
    /**
     * Build delete SQL.
     *
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return delete SQL
     */
    String buildDeleteSQL(DataRecord dataRecord, Collection<Column> conditionColumns);
    
    /**
     * Build count SQL.
     *
     * @param tableName table name
     * @return count SQL
     */
    String buildCountSQL(String tableName);
    
    /**
     * Build split by primary key range SQL.
     *
     * @param tableName table name
     * @param primaryKey primary key
     * @return split SQL
     */
    String buildSplitByPrimaryKeyRangeSQL(String tableName, String primaryKey);
}
