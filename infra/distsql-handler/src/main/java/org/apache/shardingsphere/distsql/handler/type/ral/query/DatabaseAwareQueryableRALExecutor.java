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

package org.apache.shardingsphere.distsql.handler.type.ral.query;

import org.apache.shardingsphere.distsql.statement.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

/**
 * Database Aware queryable RAL executor.
 * 
 * @param <T> type of queryable RAL statement
 */
public interface DatabaseAwareQueryableRALExecutor<T extends QueryableRALStatement> extends QueryableRALExecutor<T> {
    
    /**
     * Set current database.
     * 
     * @param currentDatabase current database
     */
    void setCurrentDatabase(ShardingSphereDatabase currentDatabase);
}
