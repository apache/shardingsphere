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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropIndexStatement;

/**
 * Drop index statement binder.
 */
public final class DropIndexStatementBinder implements SQLStatementBinder<DropIndexStatement> {
    
    @Override
    public DropIndexStatement bind(final DropIndexStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        if (!sqlStatement.getSimpleTable().isPresent()) {
            return sqlStatement;
        }
        DropIndexStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveMap.CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        result.setSimpleTable(SimpleTableSegmentBinder.bind(sqlStatement.getSimpleTable().get(), binderContext, tableBinderContexts));
        sqlStatement.getIndexes().forEach(each -> result.getIndexes().add(each));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static DropIndexStatement copy(final DropIndexStatement sqlStatement) {
        DropIndexStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getSimpleTable().ifPresent(result::setSimpleTable);
        sqlStatement.getAlgorithmType().ifPresent(result::setAlgorithmType);
        sqlStatement.getLockTable().ifPresent(result::setLockTable);
        result.setIfExists(sqlStatement.isIfExists());
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
}
