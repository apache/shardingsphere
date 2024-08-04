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
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Supported SQL check engine.
 */
public final class SupportedSQLCheckEngine {
    
    /**
     * Check SQL.
     *
     * @param rules rules
     * @param sqlStatementContext to be checked SQL statement context
     * @param schemas schemas
     * @param databaseName database name
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void checkSQL(final Collection<ShardingSphereRule> rules, final SQLStatementContext sqlStatementContext, final Map<String, ShardingSphereSchema> schemas, final String databaseName) {
        ShardingSphereSchema schema = getSchema(sqlStatementContext, schemas, databaseName);
        for (Entry<ShardingSphereRule, SupportedSQLCheckersBuilder> entry : OrderedSPILoader.getServices(SupportedSQLCheckersBuilder.class, rules).entrySet()) {
            Collection<SupportedSQLChecker> checkers = entry.getValue().getSupportedSQLCheckers();
            for (SupportedSQLChecker each : checkers) {
                if (each.isCheck(sqlStatementContext)) {
                    each.check(entry.getKey(), schema, sqlStatementContext);
                }
            }
        }
    }
    
    private ShardingSphereSchema getSchema(final SQLStatementContext sqlStatementContext, final Map<String, ShardingSphereSchema> schemas, final String databaseName) {
        ShardingSphereSchema defaultSchema = schemas.get(new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(databaseName));
        return sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getSchemaName().map(schemas::get).orElse(defaultSchema) : defaultSchema;
    }
}
