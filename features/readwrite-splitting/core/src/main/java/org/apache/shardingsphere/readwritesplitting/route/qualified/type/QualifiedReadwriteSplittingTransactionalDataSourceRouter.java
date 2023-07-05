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

package org.apache.shardingsphere.readwritesplitting.route.qualified.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.readwritesplitting.route.qualified.QualifiedReadwriteSplittingDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.route.standard.StandardReadwriteSplittingDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;

/**
 * Qualified data source transactional router for readwrite-splitting.
 */
@RequiredArgsConstructor
public final class QualifiedReadwriteSplittingTransactionalDataSourceRouter implements QualifiedReadwriteSplittingDataSourceRouter {
    
    private final ConnectionContext connectionContext;
    
    private final StandardReadwriteSplittingDataSourceRouter standardRouter = new StandardReadwriteSplittingDataSourceRouter();
    
    @Override
    public boolean isQualified(final SQLStatementContext sqlStatementContext, final ReadwriteSplittingDataSourceRule rule, final HintValueContext hintValueContext) {
        return connectionContext.getTransactionContext().isInTransaction();
    }
    
    @Override
    public String route(final ReadwriteSplittingDataSourceRule rule) {
        switch (rule.getTransactionalReadQueryStrategy()) {
            case FIXED:
                if (null == connectionContext.getTransactionContext().getReadWriteSplitReplicaRoute()) {
                    connectionContext.getTransactionContext().setReadWriteSplitReplicaRoute(standardRouter.route(rule));
                }
                return connectionContext.getTransactionContext().getReadWriteSplitReplicaRoute();
            case DYNAMIC:
                return standardRouter.route(rule);
            case PRIMARY:
            default:
                return rule.getWriteDataSource();
        }
    }
}
