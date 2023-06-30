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

package org.apache.shardingsphere.sqlfederation.distsql.handler.query;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.statement.queryable.ShowSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowSQLFederationRuleExecutorTest {
    
    @Test
    void assertSQLFederationRule() {
        ShardingSphereMetaData metaData = mockMetaData();
        ShowSQLFederationRuleExecutor executor = new ShowSQLFederationRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, mock(ShowSQLFederationRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("true"));
        assertThat(row.getCell(2), is("initialCapacity: 2000, maximumSize: 65535"));
    }
    
    @Test
    void assertGetColumnNames() {
        ShowSQLFederationRuleExecutor executor = new ShowSQLFederationRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("sql_federation_enabled"));
        assertThat(iterator.next(), is("execution_plan_cache"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        SQLFederationRule sqlFederationRule = mock(SQLFederationRule.class);
        when(sqlFederationRule.getConfiguration()).thenReturn(new SQLFederationRuleConfiguration(true, new CacheOption(2000, 65535L)));
        return new ShardingSphereMetaData(new LinkedHashMap<>(), mock(ShardingSphereResourceMetaData.class),
                new ShardingSphereRuleMetaData(Collections.singleton(sqlFederationRule)), new ConfigurationProperties(new Properties()));
    }
}
