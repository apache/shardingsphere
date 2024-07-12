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

package org.apache.shardingsphere.infra.binder.segment.combine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Map;

/**
 * Combine segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CombineSegmentBinder {
    
    /**
     * Bind combine segment.
     *
     * @param segment table segment
     * @param binderContext SQL statement binder context
     * @return bound combine segment
     */
    public static CombineSegment bind(final CombineSegment segment, final SQLStatementBinderContext binderContext) {
        ShardingSphereMetaData metaData = binderContext.getMetaData();
        String currentDatabaseName = binderContext.getCurrentDatabaseName();
        Map<String, TableSegmentBinderContext> externalTableBinderContexts = binderContext.getExternalTableBinderContexts();
        SelectStatement boundLeftSelect = new SelectStatementBinder().bind(
                segment.getLeft().getSelect(), createBinderContext(segment.getLeft().getSelect(), metaData, currentDatabaseName, externalTableBinderContexts));
        SelectStatement boundRightSelect = new SelectStatementBinder().bind(
                segment.getRight().getSelect(), createBinderContext(segment.getRight().getSelect(), metaData, currentDatabaseName, externalTableBinderContexts));
        SubquerySegment boundLeft = new SubquerySegment(segment.getLeft().getStartIndex(), segment.getLeft().getStopIndex(), segment.getLeft().getText());
        boundLeft.setSelect(boundLeftSelect);
        boundLeft.setSubqueryType(segment.getLeft().getSubqueryType());
        SubquerySegment boundRight = new SubquerySegment(segment.getRight().getStartIndex(), segment.getRight().getStopIndex(), segment.getRight().getText());
        boundRight.setSelect(boundRightSelect);
        boundRight.setSubqueryType(segment.getRight().getSubqueryType());
        return new CombineSegment(segment.getStartIndex(), segment.getStopIndex(), boundLeft, segment.getCombineType(), boundRight);
    }
    
    private static SQLStatementBinderContext createBinderContext(final SelectStatement select, final ShardingSphereMetaData metaData,
                                                                 final String currentDatabaseName, final Map<String, TableSegmentBinderContext> externalTableBinderContexts) {
        SQLStatementBinderContext result = new SQLStatementBinderContext(select, metaData, currentDatabaseName);
        result.getExternalTableBinderContexts().putAll(externalTableBinderContexts);
        return result;
    }
}
