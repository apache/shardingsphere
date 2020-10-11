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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateProcedureStatementHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding create procedure statement validator.
 */
public final class ShardingCreateProcedureStatementValidator implements ShardingStatementValidator<CreateProcedureStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<CreateProcedureStatement> sqlStatementContext,
                            final List<Object> parameters, final ShardingSphereMetaData metaData) {
        Optional<RoutineBodySegment> routineBodySegment = CreateProcedureStatementHandler.getRoutineBodySegment(sqlStatementContext.getSqlStatement());
        TableExtractor extractor = new TableExtractor();
        routineBodySegment.ifPresent(routineBody -> validateShardingTableAndTableExist(metaData, extractor.extractExistTableFromRoutineBody(routineBody)));
        routineBodySegment.ifPresent(routineBody -> validateTableNotExist(metaData, extractor.extractNotExistTableFromRoutineBody(routineBody)));
    }
    
    @Override
    public void postValidate(final CreateProcedureStatement sqlStatement, final RouteContext routeContext) {
    }
    
    private void validateShardingTableAndTableExist(final ShardingSphereMetaData metaData, final Collection<SimpleTableSegment> simpleTableSegments) {
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData().getAllTableNames().contains(tableName)) {
                throw new ShardingSphereException("Create procedure statement can not support sharding table '%s'.", tableName);
            }
            for (Map.Entry<String, Collection<String>> entry : metaData.getRuleSchemaMetaData().getUnconfiguredSchemaMetaDataMap().entrySet()) {
                if (!entry.getValue().contains(tableName)) {
                    throw new NoSuchTableException(entry.getKey(), tableName);
                }
            }
        }
    }
    
    private void validateTableNotExist(final ShardingSphereMetaData metaData, final Collection<SimpleTableSegment> simpleTableSegments) {
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (metaData.getRuleSchemaMetaData().getAllTableNames().contains(tableName)) {
                throw new TableExistsException(tableName);
            }
        }
    }
}
