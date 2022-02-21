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

package org.apache.shardingsphere.infra.distsql.query;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.spi.typed.TypedSPI;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;

/**
 * Dist SQL result set.
 */
public interface DistSQLResultSet extends TypedSPI {
    
    /**
     * Initialize data.
     * 
     * @param metaData meta data for ShardingSphere
     * @param sqlStatement SQL statement
     */
    void init(ShardingSphereMetaData metaData, SQLStatement sqlStatement);
    
    /**
     * Get result set column names.
     * 
     * @return result set column names
     */
    Collection<String> getColumnNames();
    
    /**
     * Go to next data.
     * 
     * @return true if next data exist
     */
    boolean next();
    
    /**
     * Get row data.
     * 
     * @return row data
     */
    Collection<Object> getRowData();
}
