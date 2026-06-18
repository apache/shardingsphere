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

package org.apache.shardingsphere.shadow.route.retriever;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.retriever.dml.ShadowDMLStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.hint.ShadowHintDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;

import java.util.Optional;

/**
 * Shadow data source mappings retriever factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowDataSourceMappingsRetrieverFactory {
    
    /**
     * Create new instance of shadow data source mappings retriever.
     *
     * @param queryContext query context
     * @return created instance
     */
    public static ShadowDataSourceMappingsRetriever newInstance(final QueryContext queryContext) {
        Optional<ShadowOperationType> operationType = getShadowOperationType(queryContext.getSqlStatementContext());
        return operationType.isPresent()
                ? new ShadowDMLStatementDataSourceMappingsRetriever(queryContext, operationType.get())
                : new ShadowHintDataSourceMappingsRetriever(queryContext.getHintValueContext());
    }
    
    private static Optional<ShadowOperationType> getShadowOperationType(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof InsertStatementContext) {
            return Optional.of(ShadowOperationType.INSERT);
        }
        if (sqlStatementContext instanceof DeleteStatementContext) {
            return Optional.of(ShadowOperationType.DELETE);
        }
        if (sqlStatementContext instanceof UpdateStatementContext) {
            return Optional.of(ShadowOperationType.UPDATE);
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            return Optional.of(ShadowOperationType.SELECT);
        }
        return Optional.empty();
    }
}
