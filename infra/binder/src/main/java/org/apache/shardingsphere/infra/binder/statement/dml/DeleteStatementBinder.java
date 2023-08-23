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
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.where.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.DeleteStatementHandler;

import java.util.Collections;
import java.util.Map;

/**
 * Update statement binder.
 */
public final class DeleteStatementBinder implements SQLStatementBinder<DeleteStatement> {
    
    @SneakyThrows
    @Override
    public DeleteStatement bind(final DeleteStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        DeleteStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(metaData, defaultDatabaseName, sqlStatement.getDatabaseType());
        TableSegment boundedTableSegment = TableSegmentBinder.bind(sqlStatement.getTable(), statementBinderContext, tableBinderContexts);
        result.setTable(boundedTableSegment);
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, statementBinderContext, tableBinderContexts, Collections.emptyMap())));
        DeleteStatementHandler.getOrderBySegment(sqlStatement).ifPresent(optional -> DeleteStatementHandler.setOrderBySegment(result, optional));
        DeleteStatementHandler.getLimitSegment(sqlStatement).ifPresent(optional -> DeleteStatementHandler.setLimitSegment(result, optional));
        DeleteStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> DeleteStatementHandler.setWithSegment(result, optional));
        DeleteStatementHandler.getOutputSegment(sqlStatement).ifPresent(optional -> DeleteStatementHandler.setOutputSegment(result, optional));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
