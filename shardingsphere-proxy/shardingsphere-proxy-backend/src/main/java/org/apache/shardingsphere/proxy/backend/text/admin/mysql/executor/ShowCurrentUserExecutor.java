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
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Show current user executor.
 */
@Getter
public final class ShowCurrentUserExecutor implements DatabaseAdminQueryExecutor {
    
    public static final String FUNCTION_NAME = "current_user()";
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final BackendConnection backendConnection) {
        Collection<ShardingSphereRule> rules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData().getRules();
        Optional<Grantee> grantee = rules.stream().filter(each -> each instanceof AuthorityRule)
                .map(each -> ((AuthorityRule) each).findUser(backendConnection.getGrantee())).filter(Optional::isPresent)
                .map(Optional::get).map(ShardingSphereUser::getGrantee).findFirst();
        mergedResult = new SingleLocalDataMergedResult(Collections.singleton(grantee.isPresent() ? grantee.get().toString() : ""));
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", FUNCTION_NAME, FUNCTION_NAME, Types.VARCHAR, "VARCHAR", 100, 0)));
    }
}
