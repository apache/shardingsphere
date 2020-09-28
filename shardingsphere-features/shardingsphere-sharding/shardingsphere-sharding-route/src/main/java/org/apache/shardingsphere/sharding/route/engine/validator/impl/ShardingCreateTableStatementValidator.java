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

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateTableStatementHandler;

import java.util.List;

/**
 * Sharding create table statement validator.
 */
public final class ShardingCreateTableStatementValidator implements ShardingStatementValidator<CreateTableStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, 
                            final SQLStatementContext<CreateTableStatement> sqlStatementContext, final List<Object> parameters, final ShardingSphereMetaData metaData) {
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        if (!CreateTableStatementHandler.containsNotExistClause(sqlStatementContext.getSqlStatement()) && metaData.getRuleSchemaMetaData().getAllTableNames().contains(tableName)) {
            throw new TableExistsException(tableName);
        }
    }
    
    @Override
    public void postValidate(final CreateTableStatement sqlStatement, final RouteContext routeContext) {
    }
}
