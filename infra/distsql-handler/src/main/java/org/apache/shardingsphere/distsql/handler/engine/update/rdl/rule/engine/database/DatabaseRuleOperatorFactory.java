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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type.AlterDatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type.CreateDatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type.DropDatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Database rule operator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleOperatorFactory {
    
    /**
     * Create new instance of database rule operator.
     *
     * @param contextManager context manager
     * @param executor database rule definition executor
     * @return created instance
     * @throws UnsupportedSQLOperationException if invalid database rule definition executor
     */
    @SuppressWarnings("rawtypes")
    public static DatabaseRuleOperator newInstance(final ContextManager contextManager, final DatabaseRuleDefinitionExecutor executor) {
        if (executor instanceof DatabaseRuleCreateExecutor) {
            return new CreateDatabaseRuleOperator(contextManager, (DatabaseRuleCreateExecutor) executor);
        }
        if (executor instanceof DatabaseRuleAlterExecutor) {
            return new AlterDatabaseRuleOperator(contextManager, (DatabaseRuleAlterExecutor) executor);
        }
        if (executor instanceof DatabaseRuleDropExecutor) {
            return new DropDatabaseRuleOperator(contextManager, (DatabaseRuleDropExecutor) executor);
        }
        throw new UnsupportedSQLOperationException(String.format("Cannot support RDL executor type `%s`", executor.getClass().getName()));
    }
}
