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

package org.apache.shardingsphere.infra.binder.statement.dml;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.where.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Delete statement binder.
 */
public final class DeleteStatementBinder implements SQLStatementBinder<DeleteStatement> {
    
    @Override
    public DeleteStatement bind(final DeleteStatement sqlStatement, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        return bind(sqlStatement, metaData, currentDatabaseName, Collections.emptyMap());
    }
    
    private DeleteStatement bind(final DeleteStatement sqlStatement, final ShardingSphereMetaData metaData, final String currentDatabaseName,
                                 final Map<String, TableSegmentBinderContext> externalTableBinderContexts) {
        DeleteStatement result = copy(sqlStatement);
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>();
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(sqlStatement, metaData, currentDatabaseName);
        statementBinderContext.getExternalTableBinderContexts().putAll(externalTableBinderContexts);
        result.setTable(TableSegmentBinder.bind(sqlStatement.getTable(), statementBinderContext, tableBinderContexts, Collections.emptyMap()));
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, statementBinderContext, tableBinderContexts, Collections.emptyMap())));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private DeleteStatement copy(final DeleteStatement sqlStatement) {
        DeleteStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getOrderBy().ifPresent(result::setOrderBy);
        sqlStatement.getLimit().ifPresent(result::setLimit);
        sqlStatement.getWithSegment().ifPresent(result::setWithSegment);
        sqlStatement.getOutputSegment().ifPresent(result::setOutputSegment);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
