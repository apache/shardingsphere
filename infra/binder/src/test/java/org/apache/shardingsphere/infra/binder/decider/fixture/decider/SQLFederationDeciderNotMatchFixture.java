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

package org.apache.shardingsphere.infra.binder.decider.fixture.decider;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.SQLFederationDecider;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.decider.fixture.rule.SQLFederationDeciderRuleNotMatchFixture;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

public final class SQLFederationDeciderNotMatchFixture implements SQLFederationDecider<SQLFederationDeciderRuleNotMatchFixture> {
    
    @Override
    public void decide(final SQLFederationDeciderContext deciderContext, final QueryContext queryContext, final ShardingSphereDatabase database, final SQLFederationDeciderRuleNotMatchFixture rule,
                       final ConfigurationProperties props) {
    }
    
    @Override
    public int getOrder() {
        return 10;
    }
    
    @Override
    public Class<SQLFederationDeciderRuleNotMatchFixture> getTypeClass() {
        return SQLFederationDeciderRuleNotMatchFixture.class;
    }
}
