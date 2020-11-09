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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterViewStatementHandler;

import java.util.List;
import java.util.Optional;

/**
 * Sharding alter view statement validator.
 */
public final class ShardingAlterViewStatementValidator extends ShardingDDLStatementValidator<AlterViewStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<AlterViewStatement> sqlStatementContext, 
                            final List<Object> parameters, final ShardingSphereSchema schema) {
        Optional<SelectStatement> selectStatement = AlterViewStatementHandler.getSelectStatement(sqlStatementContext.getSqlStatement());
        selectStatement.ifPresent(select -> {
            TableExtractor extractor = new TableExtractor();
            extractor.extractTablesFromSelect(select);
            validateShardingTable(schema, extractor.getRewriteTables());
        });
    }
    
    @Override
    public void postValidate(final AlterViewStatement sqlStatement, final RouteContext routeContext) {
    }
}
