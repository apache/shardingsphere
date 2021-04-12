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

package org.apache.shardingsphere.infra.executor.check;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.List;
import java.util.Map;

/**
 * SQL checker.
 * 
 */
public interface SQLChecker<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Check schema.
     *
     * @param schemaName schema name
     * @param grantee grantee
     * @param rule rule
     * @return check result
     */
    boolean check(String schemaName, Grantee grantee, T rule);
    
    /**
     * Check SQL.
     * 
     * @param sqlStatement SQL statement
     * @param parameters SQL parameters
     * @param grantee grantee
     * @param currentSchema current schema
     * @param metaDataMap meta data map
     * @param rule rule
     * @return SQL check result
     */
    SQLCheckResult check(SQLStatement sqlStatement, List<Object> parameters, Grantee grantee, String currentSchema, Map<String, ShardingSphereMetaData> metaDataMap, T rule);
}
