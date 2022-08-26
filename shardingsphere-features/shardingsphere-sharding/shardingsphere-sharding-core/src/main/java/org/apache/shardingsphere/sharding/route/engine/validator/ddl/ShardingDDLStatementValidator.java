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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl;

import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;

import java.util.Collection;

/**
 * Sharding ddl statement validator.
 */
public abstract class ShardingDDLStatementValidator<T extends DDLStatement> implements ShardingStatementValidator<T> {
    
    /**
     * Validate sharding table.
     *
     * @param shardingRule sharding rule
     * @param operation operation
     * @param tables tables
     */
    protected void validateShardingTable(final ShardingRule shardingRule, final String operation, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (shardingRule.isShardingTable(tableName)) {
                throw new UnsupportedShardingOperationException(operation, tableName);
            }
        }
    }
    
    /**
     * Validate table exist.
     *
     * @param schema ShardingSphere schema
     * @param tables tables
     */
    protected void validateTableExist(final ShardingSphereSchema schema, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (!schema.containsTable(tableName)) {
                throw new NoSuchTableException(tableName);
            }
        }
    }
    
    /**
     * Validate table not exist.
     *
     * @param schema ShardingSphere schema
     * @param tables tables
     */
    protected void validateTableNotExist(final ShardingSphereSchema schema, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (schema.containsTable(tableName)) {
                throw new TableExistsException(tableName);
            }
        }
    }
    
    /**
     * Judge whether route unit and data node are different size or not.
     * 
     * @param shardingRule sharding rule
     * @param routeContext route context
     * @param tableName table name
     * @return whether route unit and data node are different size or not
     */
    protected boolean isRouteUnitDataNodeDifferentSize(final ShardingRule shardingRule, final RouteContext routeContext, final String tableName) {
        return (shardingRule.isShardingTable(tableName) || shardingRule.isBroadcastTable(tableName))
                && shardingRule.getTableRule(tableName).getActualDataNodes().size() != routeContext.getRouteUnits().size();
    }
    
    /**
     * Judge whether schema contains index or not.
     *
     * @param schema ShardingSphere schema
     * @param index index
     * @return whether schema contains index or not
     */
    protected boolean isSchemaContainsIndex(final ShardingSphereSchema schema, final IndexSegment index) {
        return schema.getAllTableNames().stream().anyMatch(each -> schema.get(each).getIndexes().containsKey(index.getIndexName().getIdentifier().getValue()));
    }
}
