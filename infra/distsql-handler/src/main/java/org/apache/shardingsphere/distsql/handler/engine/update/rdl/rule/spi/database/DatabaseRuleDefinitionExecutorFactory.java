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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Optional;

/**
 * Database rule definition executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleDefinitionExecutorFactory {
    
    /**
     * Find instance.
     *
     * @param sqlStatement SQL statement
     * @param database database
     * @return found instance 
     */
    @SuppressWarnings("rawtypes")
    public static Optional<DatabaseRuleDefinitionExecutor> findInstance(final DistSQLStatement sqlStatement, final ShardingSphereDatabase database) {
        Optional<DatabaseRuleDefinitionExecutor> result = TypedSPILoader.findService(DatabaseRuleDefinitionExecutor.class, sqlStatement.getClass());
        result.ifPresent(optional -> setAttributes(database, optional));
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setAttributes(final ShardingSphereDatabase database, final DatabaseRuleDefinitionExecutor executor) {
        executor.setDatabase(database);
        Optional<ShardingSphereRule> rule = database.getRuleMetaData().findSingleRule(executor.getRuleClass());
        executor.setRule(rule.orElse(null));
    }
}
