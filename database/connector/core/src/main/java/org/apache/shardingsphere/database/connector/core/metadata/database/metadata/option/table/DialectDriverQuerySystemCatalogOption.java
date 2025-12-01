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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table;

/**
 * Dialect driver query system catalog option.
 */
public interface DialectDriverQuerySystemCatalogOption {
    
    /**
     * Whether system catalog query expressions.
     *
     * @param projectionExpression projection expression
     * @return is query expressions or not
     */
    boolean isSystemCatalogQueryExpressions(String projectionExpression);
    
    /**
     * Whether system table.
     *
     * @param tableName table name
     * @return is system table or not
     */
    boolean isSystemTable(String tableName);
    
    /**
     * Whether database data table.
     *
     * @param tableName table name
     * @return is database data table or not
     */
    boolean isDatabaseDataTable(String tableName);
    
    /**
     * Whether table data table.
     *
     * @param tableName table name
     * @return is table data table or not
     */
    boolean isTableDataTable(String tableName);
    
    /**
     * Whether role data table.
     *
     * @param tableName table name
     * @return is role data table or not
     */
    boolean isRoleDataTable(String tableName);
    
    /**
     * Get dat compatibility.
     *
     * @return dat compatibility
     */
    String getDatCompatibility();
}
