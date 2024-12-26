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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.engine.segment.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.SQLServerAlterIndexStatement;

/**
 * Alter index statement binder.
 */
public class AlterIndexStatementBinder implements SQLStatementBinder<AlterIndexStatement> {
    
    @Override
    public AlterIndexStatement bind(final AlterIndexStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        if (sqlStatement instanceof SQLServerAlterIndexStatement) {
            SQLServerAlterIndexStatement source = (SQLServerAlterIndexStatement) sqlStatement;
            if (!source.getSimpleTable().isPresent()) {
                return sqlStatement;
            }
            SQLServerAlterIndexStatement result = copy(source);
            result.setSimpleTable(SimpleTableSegmentBinder.bind(source.getSimpleTable().get(), binderContext, null));
            return result;
        }
        return sqlStatement;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static SQLServerAlterIndexStatement copy(final SQLServerAlterIndexStatement sqlStatement) {
        SQLServerAlterIndexStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getIndex().ifPresent(result::setIndex);
        sqlStatement.getSimpleTable().ifPresent(result::setSimpleTable);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
}
