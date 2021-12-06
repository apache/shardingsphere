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

package org.apache.shardingsphere.infra.binder.segment.select.subquery.engine;

import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.subquery.SubqueryTableContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Subquery table context engine.
 */
public final class SubqueryTableContextEngine {
    
    /**
     * Create subquery table contexts.
     *
     * @param subqueryContext subquery context
     * @param alias subquery alias
     * @return subquery table context collection
     */
    public Collection<SubqueryTableContext> createSubqueryTableContexts(final SelectStatementContext subqueryContext, final String alias) {
        Collection<SubqueryTableContext> result = new LinkedList<>();
        List<String> columnNames = subqueryContext.getProjectionsContext().getExpandProjections().stream()
                .filter(each -> each instanceof ColumnProjection).map(each -> ((ColumnProjection) each).getName()).collect(Collectors.toList());
        for (String each : subqueryContext.getTablesContext().getTableNames()) {
            result.add(new SubqueryTableContext(each, alias, columnNames));
        }
        return result;
    }
}
