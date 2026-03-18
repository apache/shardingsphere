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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Create procedure statement context.
 */
@Getter
public final class CreateProcedureStatementContext implements SQLStatementContext {
    
    private final CreateProcedureStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    public CreateProcedureStatementContext(final CreateProcedureStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        Optional<RoutineBodySegment> routineBodySegment = sqlStatement.getRoutineBody();
        Collection<SimpleTableSegment> tables = routineBodySegment.map(optional -> new TableExtractor().extractExistTableFromRoutineBody(optional)).orElseGet(Collections::emptyList);
        tablesContext = new TablesContext(tables);
    }
}
