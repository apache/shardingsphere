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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateProcedureStatementHandler;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Sharding create procedure statement validator.
 */
public final class ShardingCreateProcedureStatementValidator extends ShardingDDLStatementValidator {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                            final List<Object> params, final ShardingSphereDatabase database, final ConfigurationProperties props) {
        CreateProcedureStatement createProcedureStatement = (CreateProcedureStatement) sqlStatementContext.getSqlStatement();
        Optional<RoutineBodySegment> routineBodySegment = CreateProcedureStatementHandler.getRoutineBodySegment(createProcedureStatement);
        if (!routineBodySegment.isPresent()) {
            return;
        }
        TableExtractor extractor = new TableExtractor();
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), database.getName());
        ShardingSphereSchema schema = createProcedureStatement.getProcedureName().flatMap(optional -> optional.getOwner()
                .map(owner -> database.getSchema(owner.getIdentifier().getValue()))).orElseGet(() -> database.getSchema(defaultSchemaName));
        Collection<SimpleTableSegment> existTables = extractor.extractExistTableFromRoutineBody(routineBodySegment.get());
        validateShardingTable(shardingRule, "CREATE PROCEDURE", existTables);
        validateTableExist(schema, existTables);
        Collection<SimpleTableSegment> notExistTables = extractor.extractNotExistTableFromRoutineBody(routineBodySegment.get());
        validateTableNotExist(schema, notExistTables);
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext,
                             final List<Object> params, final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
    }
}
