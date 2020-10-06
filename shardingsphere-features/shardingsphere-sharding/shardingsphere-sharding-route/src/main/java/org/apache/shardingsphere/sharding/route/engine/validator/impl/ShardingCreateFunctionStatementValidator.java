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

package org.apache.shardingsphere.sharding.route.engine.validator.impl;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateFunctionStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Sharding create function statement validator.
 */
public final class ShardingCreateFunctionStatementValidator implements ShardingStatementValidator<CreateFunctionStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<CreateFunctionStatement> sqlStatementContext,
                            final List<Object> parameters, final ShardingSphereMetaData metaData) {
        for (SimpleTableSegment each : ((CreateFunctionStatementContext) sqlStatementContext).getExistTables()) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData().getAllTableNames().contains(tableName)) {
                throw new ShardingSphereException("Create function statement can not support sharding table '%s'.", tableName);
            }
            for (Map.Entry<String, Collection<String>> entry : metaData.getRuleSchemaMetaData().getUnconfiguredSchemaMetaDataMap().entrySet()) {
                if (!entry.getValue().contains(tableName)) {
                    throw new NoSuchTableException(entry.getKey(), tableName);
                }
            }
        }
        for (SimpleTableSegment each : ((CreateFunctionStatementContext) sqlStatementContext).getNotExistTables()) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (metaData.getRuleSchemaMetaData().getAllTableNames().contains(tableName)) {
                throw new TableExistsException(tableName);
            }
        }
    }
    
    @Override
    public void postValidate(final CreateFunctionStatement sqlStatement, final RouteContext routeContext) {
    }
}
