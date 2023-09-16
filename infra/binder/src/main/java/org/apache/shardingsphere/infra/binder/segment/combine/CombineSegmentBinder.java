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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * Combine segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CombineSegmentBinder {
    
    /**
     * Bind combine segment with metadata.
     *
     * @param segment table segment
     * @param statementBinderContext statement binder context
     * @return bounded combine segment
     */
    public static CombineSegment bind(final CombineSegment segment, final SQLStatementBinderContext statementBinderContext) {
        ShardingSphereMetaData metaData = statementBinderContext.getMetaData();
        String defaultDatabaseName = statementBinderContext.getDefaultDatabaseName();
        SelectStatement boundedLeftSelect = new SelectStatementBinder().bindWithExternalTableContexts(segment.getLeft(), metaData, defaultDatabaseName,
                statementBinderContext.getExternalTableBinderContexts());
        SelectStatement boundedRightSelect = new SelectStatementBinder().bindWithExternalTableContexts(segment.getRight(), metaData, defaultDatabaseName,
                statementBinderContext.getExternalTableBinderContexts());
        return new CombineSegment(segment.getStartIndex(), segment.getStopIndex(), boundedLeftSelect, segment.getCombineType(), boundedRightSelect);
    }
}
