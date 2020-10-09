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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.sql.LogicSQL;
import org.apache.shardingsphere.infra.datetime.DatetimeService;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;

/**
 * Sharding condition engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingConditionEngineFactory {
    
    static {
        ShardingSphereServiceLoader.register(DatetimeService.class);
    }
    
    /**
     * Create new instance of sharding condition engine.
     *
     * @param logicSQL logic SQL
     * @param rule sharding rule 
     * @return sharding condition engine
     */
    public static ShardingConditionEngine<?> createShardingConditionEngine(final LogicSQL logicSQL, final ShardingRule rule) {
        SchemaMetaData schemaMetaData = logicSQL.getSchema().getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData();
        return logicSQL.getSqlStatementContext() instanceof InsertStatementContext
                ? new InsertClauseShardingConditionEngine(rule, schemaMetaData) : new WhereClauseShardingConditionEngine(rule, schemaMetaData);
    }
}
