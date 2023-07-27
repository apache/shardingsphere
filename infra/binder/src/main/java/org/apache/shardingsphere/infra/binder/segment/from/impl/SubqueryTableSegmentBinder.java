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

package org.apache.shardingsphere.infra.binder.segment.from.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Map;

/**
 * Subquery table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryTableSegmentBinder {
    
    /**
     * Bind subquery table segment with metadata.
     *
     * @param segment join table segment
     * @param metaData meta data
     * @param defaultDatabaseName default database name
     * @param tableBinderContexts table binder contexts
     * @return bounded subquery table segment
     */
    public static SubqueryTableSegment bind(final SubqueryTableSegment segment, final ShardingSphereMetaData metaData, final String defaultDatabaseName,
                                            final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        SelectStatement boundedSelect = new SelectStatementBinder().bind(segment.getSubquery().getSelect(), metaData, defaultDatabaseName);
        SubquerySegment boundedSubquerySegment = new SubquerySegment(segment.getSubquery().getStartIndex(), segment.getSubquery().getStopIndex(), boundedSelect);
        boundedSubquerySegment.setSubqueryType(segment.getSubquery().getSubqueryType());
        SubqueryTableSegment result = new SubqueryTableSegment(boundedSubquerySegment);
        segment.getAliasSegment().ifPresent(result::setAlias);
        IdentifierValue subqueryTableName = segment.getAliasSegment().map(AliasSegment::getIdentifier).orElseGet(() -> new IdentifierValue(""));
        tableBinderContexts.put(subqueryTableName.getValue(), new TableSegmentBinderContext(boundedSelect.getProjections().getProjections()));
        return result;
    }
}
