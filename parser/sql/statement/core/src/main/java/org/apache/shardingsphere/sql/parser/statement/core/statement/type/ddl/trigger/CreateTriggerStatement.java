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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureBodyEndNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Create trigger statement.
 */
@Getter
@Setter
public final class CreateTriggerStatement extends DDLStatement {
    
    private FunctionNameSegment triggerName;

    private final List<SQLStatementSegment> sqlStatements = new ArrayList<>();

    private final List<ProcedureCallNameSegment> procedureCallNames = new ArrayList<>();

    private final List<ProcedureBodyEndNameSegment> triggerBodyEndNameSegments = new ArrayList<>();

    private final List<ExpressionSegment> dynamicSqlStatementExpressions = new ArrayList<>();

    private final Collection<SimpleTableSegment> tables = new LinkedList<>();

    private final SQLStatementAttributes attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(tables));

    public CreateTriggerStatement(final DatabaseType databaseType) {
        super(databaseType);
    }

    /**
     * Get trigger name segment.
     *
     * @return trigger name segment
     */
    public Optional<FunctionNameSegment> getTriggerName() {
        return Optional.ofNullable(triggerName);
    }

    /**
     * Set trigger table.
     *
     * @param table trigger table
     */
    public void setTable(final SimpleTableSegment table) {
        tables.clear();
        if (null != table) {
            tables.add(table);
        }
    }
}
