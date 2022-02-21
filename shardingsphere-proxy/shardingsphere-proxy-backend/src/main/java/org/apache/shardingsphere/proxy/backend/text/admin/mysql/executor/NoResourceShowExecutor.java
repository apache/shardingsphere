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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.select.projection.engine.ProjectionEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * No resource show executor.
 */
@Getter
@RequiredArgsConstructor
public final class NoResourceShowExecutor implements DatabaseAdminQueryExecutor {
    
    private MergedResult mergedResult;
    
    private final SelectStatement sqlStatement;
    
    private Collection<Object> expressions = Collections.emptyList();
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        TableSegment tableSegment = sqlStatement.getFrom();
        expressions = sqlStatement.getProjections().getProjections().stream().filter(each -> !(each instanceof ShorthandProjectionSegment))
                .map(each -> new ProjectionEngine(null, null).createProjection(tableSegment, each))
                .filter(Optional::isPresent).map(each -> each.get().getAlias().isPresent() ? each.get().getAlias().get() : each.get().getExpression())
                .collect(Collectors.toCollection(LinkedList::new));
        mergedResult = new TransparentMergedResult(getQueryResult());
    }
    
    private QueryResult getQueryResult() {
        List<MemoryQueryResultDataRow> rows = new LinkedList<>();
        if (expressions.isEmpty()) {
            rows.add(new MemoryQueryResultDataRow(Collections.singletonList("")));
            return new RawMemoryQueryResult(getQueryResultMetaData(), rows);
        }
        ArrayList<Object> row = new ArrayList<>(expressions);
        row.replaceAll(each -> "");
        rows.add(new MemoryQueryResultDataRow(row));
        return new RawMemoryQueryResult(getQueryResultMetaData(), rows);
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        if (expressions.isEmpty()) {
            RawQueryResultColumnMetaData defaultColumnMetaData = new RawQueryResultColumnMetaData("", "", "", Types.VARCHAR, "VARCHAR", 100, 0);
            return new RawQueryResultMetaData(Collections.singletonList(defaultColumnMetaData));
        }
        LinkedList<RawQueryResultColumnMetaData> raws = expressions.stream().map(each -> new RawQueryResultColumnMetaData("", each.toString(), each.toString(), Types.VARCHAR, "VARCHAR", 100, 0))
                .collect(Collectors.toCollection(LinkedList::new));
        return new RawQueryResultMetaData(raws);
    }
}
