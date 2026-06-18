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

package org.apache.shardingsphere.sqlfederation.engine.fixture.decider;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sqlfederation.engine.fixture.rule.SQLFederationDeciderRuleMatchFixture;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.util.Collection;
import java.util.List;

public final class SQLFederationDeciderMatchFixture implements SQLFederationDecider<SQLFederationDeciderRuleMatchFixture> {
    
    @Override
    public boolean decide(final SQLStatementContext sqlStatementContext, final List<Object> parameters, final RuleMetaData globalRuleMetaData,
                          final ShardingSphereDatabase database, final SQLFederationDeciderRuleMatchFixture rule, final Collection<DataNode> includedDataNodes) {
        return true;
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<SQLFederationDeciderRuleMatchFixture> getTypeClass() {
        return SQLFederationDeciderRuleMatchFixture.class;
    }
}
