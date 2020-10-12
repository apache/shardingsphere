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

package org.apache.shardingsphere.infra.binder.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateProcedureStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Create procedure statement context.
 */
@Getter
public final class CreateProcedureStatementContext extends CommonSQLStatementContext<CreateProcedureStatement> {
    
    private final TablesContext tablesContext;
    
    public CreateProcedureStatementContext(final CreateProcedureStatement sqlStatement) {
        super(sqlStatement);
        Optional<RoutineBodySegment> routineBodySegment = CreateProcedureStatementHandler.getRoutineBodySegment(sqlStatement);
        Collection<SimpleTableSegment> tables = routineBodySegment.map(bodySegment -> new TableExtractor().extractExistTableFromRoutineBody(bodySegment)).orElse(Collections.emptyList());
        tablesContext = new TablesContext(tables);
    }
}
