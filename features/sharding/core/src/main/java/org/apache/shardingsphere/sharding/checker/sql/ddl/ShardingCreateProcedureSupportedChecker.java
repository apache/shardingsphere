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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateProcedureStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.common.ShardingSupportedCommonChecker;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * Create procedure supported checker for sharding.
 */
public final class ShardingCreateProcedureSupportedChecker implements SupportedSQLChecker<CreateProcedureStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CreateProcedureStatementContext;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final CreateProcedureStatementContext sqlStatementContext) {
        CreateProcedureStatement createProcedureStatement = sqlStatementContext.getSqlStatement();
        Optional<RoutineBodySegment> routineBodySegment = createProcedureStatement.getRoutineBody();
        if (!routineBodySegment.isPresent()) {
            return;
        }
        TableExtractor extractor = new TableExtractor();
        Collection<SimpleTableSegment> existTables = extractor.extractExistTableFromRoutineBody(routineBodySegment.get());
        ShardingSupportedCommonChecker.checkShardingTable(rule, "CREATE PROCEDURE", existTables);
        ShardingSphereSchema schema = createProcedureStatement.getProcedureName().flatMap(optional -> optional.getOwner()
                .map(owner -> database.getSchema(owner.getIdentifier().getValue()))).orElse(currentSchema);
        ShardingSupportedCommonChecker.checkTableExist(schema, existTables);
        ShardingSupportedCommonChecker.checkTableNotExist(schema, extractor.extractNotExistTableFromRoutineBody(routineBodySegment.get()));
    }
}
