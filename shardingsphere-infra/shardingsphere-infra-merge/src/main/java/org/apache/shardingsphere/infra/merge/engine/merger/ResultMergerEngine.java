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

package org.apache.shardingsphere.infra.merge.engine.merger;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine;

/**
 * Result merger engine.
 *
 * @param <T> type of rule
 */
public interface ResultMergerEngine<T extends ShardingSphereRule> extends ResultProcessEngine<T> {
    
    /**
     * Create new instance of result merger engine.
     * 
     * @param databaseType database type
     * @param rule rule
     * @param props ShardingSphere properties
     * @param sqlStatementContext SQL statement context
     * @return new instance of result merger engine
     */
    ResultMerger newInstance(DatabaseType databaseType, T rule, ConfigurationProperties props, SQLStatementContext<?> sqlStatementContext);
}
