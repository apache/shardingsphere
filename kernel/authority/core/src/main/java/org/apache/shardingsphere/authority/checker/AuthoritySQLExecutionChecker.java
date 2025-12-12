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

package org.apache.shardingsphere.authority.checker;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.checker.SQLExecutionChecker;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.query.QueryContext;

/**
 * Authority SQL execution checker.
 */
public final class AuthoritySQLExecutionChecker implements SQLExecutionChecker {
    
    @Override
    public void check(final Grantee grantee, final QueryContext queryContext, final ShardingSphereDatabase database) {
        AuthorityRule authorityRule = queryContext.getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        ShardingSpherePreconditions.checkState(new AuthorityChecker(authorityRule, grantee).isAuthorized(database.getName()), () -> new UnknownDatabaseException(database.getName()));
    }
}
