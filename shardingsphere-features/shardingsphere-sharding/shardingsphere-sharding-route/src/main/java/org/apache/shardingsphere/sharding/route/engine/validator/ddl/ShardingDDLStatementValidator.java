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

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
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
     * @param schema ShardingSphere schema
     * @param tables tables
     */
    protected void validateShardingTable(final ShardingSphereSchema schema, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (schema.getAllTableNames().contains(tableName)) {
                throw new ShardingSphereException("Can not support sharding table '%s'.", tableName);
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
}
