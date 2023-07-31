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

import java.util.Map;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.segment.combine.CombineSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * Select statement binder.
 */
public final class SelectStatementBinder implements SQLStatementBinder<SelectStatement> {
    
    @Override
    public SelectStatement bind(final SelectStatement sqlStatement, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        TableSegment boundedTableSegment = TableSegmentBinder.bind(sqlStatement.getFrom(), metaData, defaultDatabaseName, sqlStatement.getDatabaseType(), tableBinderContexts);
        sqlStatement.setFrom(boundedTableSegment);
        sqlStatement.getCombine().ifPresent(optional -> sqlStatement.setCombine(CombineSegmentBinder.bind(optional, metaData, defaultDatabaseName)));
        sqlStatement.setProjections(ProjectionsSegmentBinder.bind(sqlStatement.getProjections(), boundedTableSegment, tableBinderContexts));
        // TODO support other segment bind in select statement
        return sqlStatement;
    }
}
