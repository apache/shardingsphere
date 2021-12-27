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

package org.apache.shardingsphere.shadow.distsql.handler.query;

import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRuleStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Result set for shadow rule status.
 */
public final class ShadowRuleStatusQueryResultSet implements DistSQLResultSet {
    
    private static final String STATUS = "status";
    
    private Iterator<Boolean> data;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ShadowRuleConfiguration> rule = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShadowRuleConfiguration).map(each -> (ShadowRuleConfiguration) each).findAny();
        rule.ifPresent(configuration -> data = Collections.singleton(configuration.isEnable()).iterator());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Collections.singletonList(STATUS);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return Collections.singletonList(data.next() ? "enabled" : "disabled");
    }
    
    @Override
    public String getType() {
        return ShowShadowRuleStatusStatement.class.getCanonicalName();
    }
}
