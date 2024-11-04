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

package org.apache.shardingsphere.shadow.route.finder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.finder.dml.ShadowDeleteStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.finder.dml.ShadowInsertStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.finder.dml.ShadowSelectStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.finder.dml.ShadowUpdateStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.finder.other.ShadowNonDMLStatementDataSourceMappingsFinder;

/**
 * Shadow data source mappings finder factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowDataSourceMappingsFinderFactory {
    
    /**
     * Create new instance of shadow data source mappings finder.
     *
     * @param queryContext query context
     * @return created instance
     */
    public static ShadowDataSourceMappingsFinder newInstance(final QueryContext queryContext) {
        if (queryContext.getSqlStatementContext() instanceof InsertStatementContext) {
            return new ShadowInsertStatementDataSourceMappingsFinder((InsertStatementContext) queryContext.getSqlStatementContext(), queryContext.getHintValueContext());
        }
        if (queryContext.getSqlStatementContext() instanceof DeleteStatementContext) {
            return new ShadowDeleteStatementDataSourceMappingsFinder((DeleteStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
        }
        if (queryContext.getSqlStatementContext() instanceof UpdateStatementContext) {
            return new ShadowUpdateStatementDataSourceMappingsFinder((UpdateStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
        }
        if (queryContext.getSqlStatementContext() instanceof SelectStatementContext) {
            return new ShadowSelectStatementDataSourceMappingsFinder((SelectStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
        }
        return new ShadowNonDMLStatementDataSourceMappingsFinder(queryContext.getHintValueContext());
    }
}
