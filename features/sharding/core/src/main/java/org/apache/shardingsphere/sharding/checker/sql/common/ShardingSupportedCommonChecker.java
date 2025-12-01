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

package org.apache.shardingsphere.sharding.checker.sql.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.util.Collection;

/**
 * Sharding supported common checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSupportedCommonChecker {
    
    /**
     * Check sharding table.
     *
     * @param shardingRule sharding rule
     * @param operation operation
     * @param tables tables
     * @throws UnsupportedShardingOperationException unsupported sharding operation exception
     */
    public static void checkShardingTable(final ShardingRule shardingRule, final String operation, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(!shardingRule.isShardingTable(tableName), () -> new UnsupportedShardingOperationException(operation, tableName));
        }
    }
    
    /**
     * Check table exist.
     *
     * @param schema ShardingSphere schema
     * @param tables tables
     * @throws NoSuchTableException no such table exception
     */
    public static void checkTableExist(final ShardingSphereSchema schema, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new NoSuchTableException(tableName));
        }
    }
    
    /**
     * Check table not exist.
     *
     * @param schema ShardingSphere schema
     * @param tables tables
     * @throws TableExistsException table exists exception
     */
    public static void checkTableNotExist(final ShardingSphereSchema schema, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(!schema.containsTable(tableName), () -> new TableExistsException(tableName));
        }
    }
    
    /**
     * Check multiple table.
     *
     * @param shardingRule sharding rule
     * @param sqlStatementContext sqlStatementContext
     */
    public static void checkMultipleTable(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        boolean isAllShardingTables = shardingRule.isAllShardingTables(tableNames) && (1 == tableNames.size() || shardingRule.isAllConfigBindingTables(tableNames));
        boolean isAllSingleTables = !shardingRule.containsShardingTable(tableNames);
        ShardingSpherePreconditions.checkState(isAllShardingTables || isAllSingleTables, () -> new DMLWithMultipleShardingTablesException(tableNames));
    }
}
