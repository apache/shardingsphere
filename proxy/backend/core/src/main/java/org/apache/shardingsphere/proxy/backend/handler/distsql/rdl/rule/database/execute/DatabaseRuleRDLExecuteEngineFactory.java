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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLAlterExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLCreateExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLDropExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLExecutor;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute.type.AlterDatabaseRuleRDLExecuteEngine;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute.type.CreateDatabaseRuleRDLExecuteEngine;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.database.execute.type.DropDatabaseRuleRDLExecuteEngine;

/**
 * Database rule RDL execute engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleRDLExecuteEngineFactory {
    
    /**
     * Create new instance of database RDL execute engine.
     * @param executor database rule RDL executor
     * @return created instance
     * @throws UnsupportedSQLOperationException if invalid database rule RDL executor
     */
    @SuppressWarnings("rawtypes")
    public static DatabaseRuleRDLExecuteEngine newInstance(final DatabaseRuleRDLExecutor executor) {
        if (executor instanceof DatabaseRuleRDLCreateExecutor) {
            return new CreateDatabaseRuleRDLExecuteEngine((DatabaseRuleRDLCreateExecutor) executor);
        }
        if (executor instanceof DatabaseRuleRDLAlterExecutor) {
            return new AlterDatabaseRuleRDLExecuteEngine((DatabaseRuleRDLAlterExecutor) executor);
        }
        if (executor instanceof DatabaseRuleRDLDropExecutor) {
            return new DropDatabaseRuleRDLExecuteEngine((DatabaseRuleRDLDropExecutor) executor);
        }
        throw new UnsupportedSQLOperationException(String.format("Cannot support RDL executor type `%s`", executor.getClass().getName()));
    }
}
