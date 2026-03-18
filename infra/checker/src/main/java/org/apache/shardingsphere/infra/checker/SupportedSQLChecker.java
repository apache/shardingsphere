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

package org.apache.shardingsphere.infra.checker;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

/**
 * Supported SQL checker.
 *
 * @param <T> type of SQL statement context
 * @param <R> type of ShardingSphere rule
 */
public interface SupportedSQLChecker<T extends SQLStatementContext, R extends ShardingSphereRule> {
    
    /**
     * Judge whether to need check SQL.
     *
     * @param sqlStatementContext to be checked SQL statement context
     * @return check SQL or not
     */
    boolean isCheck(SQLStatementContext sqlStatementContext);
    
    /**
     * Check SQL.
     *
     * @param rule rule
     * @param database database
     * @param currentSchema current schema
     * @param sqlStatementContext to be checked SQL statement context
     */
    void check(R rule, ShardingSphereDatabase database, ShardingSphereSchema currentSchema, T sqlStatementContext);
}
