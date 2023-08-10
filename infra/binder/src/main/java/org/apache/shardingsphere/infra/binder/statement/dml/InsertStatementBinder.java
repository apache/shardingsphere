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
import org.apache.shardingsphere.infra.binder.segment.expression.impl.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

/**
 * Select statement binder.
 */
public final class InsertStatementBinder implements SQLStatementBinder<InsertStatement> {
    
    @SneakyThrows
    @Override
    public InsertStatement bind(final InsertStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        InsertStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.setTable(sqlStatement.getTable());
        sqlStatement.getInsertColumns().ifPresent(result::setInsertColumns);
        sqlStatement.getInsertSelect().ifPresent(optional -> result.setInsertSelect(SubquerySegmentBinder.bind(optional, metaData, defaultDatabaseName)));
        result.getValues().addAll(sqlStatement.getValues());
        InsertStatementHandler.getOnDuplicateKeyColumnsSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOnDuplicateKeyColumnsSegment(result, optional));
        InsertStatementHandler.getSetAssignmentSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setSetAssignmentSegment(result, optional));
        InsertStatementHandler.getWithSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setWithSegment(result, optional));
        InsertStatementHandler.getOutputSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setOutputSegment(result, optional));
        InsertStatementHandler.getInsertMultiTableElementSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setInsertMultiTableElementSegment(result, optional));
        InsertStatementHandler.getReturningSegment(sqlStatement).ifPresent(optional -> InsertStatementHandler.setReturningSegment(result, optional));
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
