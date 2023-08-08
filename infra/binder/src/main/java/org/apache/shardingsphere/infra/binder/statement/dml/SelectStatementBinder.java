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
import org.apache.shardingsphere.infra.binder.segment.combine.CombineSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.where.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.Map;

/**
 * Select statement binder.
 */
public final class SelectStatementBinder implements SQLStatementBinder<SelectStatement> {
    
    @SneakyThrows
    @Override
    public SelectStatement bind(final SelectStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        SelectStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        TableSegment boundedTableSegment = TableSegmentBinder.bind(sqlStatement.getFrom(), metaData, defaultDatabaseName, sqlStatement.getDatabaseType(), tableBinderContexts);
        result.setFrom(boundedTableSegment);
        result.setProjections(ProjectionsSegmentBinder.bind(sqlStatement.getProjections(), metaData, defaultDatabaseName, boundedTableSegment, tableBinderContexts));
        // TODO support other segment bind in select statement
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, metaData, defaultDatabaseName)));
        sqlStatement.getGroupBy().ifPresent(result::setGroupBy);
        sqlStatement.getHaving().ifPresent(result::setHaving);
        sqlStatement.getOrderBy().ifPresent(result::setOrderBy);
        sqlStatement.getCombine().ifPresent(optional -> result.setCombine(CombineSegmentBinder.bind(optional, metaData, defaultDatabaseName)));
        SelectStatementHandler.getLimitSegment(sqlStatement).ifPresent(optional -> SelectStatementHandler.setLimitSegment(result, optional));
        SelectStatementHandler.getLockSegment(sqlStatement).ifPresent(optional -> SelectStatementHandler.setLockSegment(result, optional));
        SelectStatementHandler.getWindowSegment(sqlStatement).ifPresent(optional -> SelectStatementHandler.setWindowSegment(result, optional));
        SelectStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> SelectStatementHandler.setWithSegment(result, optional));
        SelectStatementHandler.getModelSegment(sqlStatement).ifPresent(optional -> SelectStatementHandler.setModelSegment(result, optional));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
