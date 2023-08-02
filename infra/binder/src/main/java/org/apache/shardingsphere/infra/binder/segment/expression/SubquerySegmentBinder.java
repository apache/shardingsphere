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

package org.apache.shardingsphere.infra.binder.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * Subquery segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubquerySegmentBinder {
    
    /**
     * Bind subquery segment with metadata.
     *
     * @param segment subquery segment
     * @param metaData metaData
     * @param defaultDatabaseName default database name
     * @return bounded subquery segment
     */
    public static SubquerySegment bind(final SubquerySegment segment, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        SelectStatement boundedSelectStatement = new SelectStatementBinder().bind(segment.getSelect(), metaData, defaultDatabaseName);
        SubquerySegment result = new SubquerySegment(segment.getStartIndex(), segment.getStopIndex(), boundedSelectStatement);
        result.setSubqueryType(segment.getSubqueryType());
        return result;
    }
}
