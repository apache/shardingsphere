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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show;

import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowSQLParserRuleExecutor;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowSQLParserRuleExecutorTest {

    @Test
    public void assertSQLParserRule() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData()).thenReturn(getGlobalRuleMetaData());
        ProxyContext.getInstance().init(contextManager);
        ShowSQLParserRuleExecutor executor = new ShowSQLParserRuleExecutor();
        executor.execute();
        executor.next();
        QueryResponseRow queryResponseRow = executor.getQueryResponseRow();
        List<Object> data = new ArrayList<>(queryResponseRow.getData());
        assertThat(data.size(), is(3));
        assertThat(data.get(0), is("true"));
        String parseTreeCache = String.valueOf(data.get(1));
        assertThat(parseTreeCache, containsString("\"initialCapacity\":128"));
        assertThat(parseTreeCache, containsString("\"maximumSize\":1024"));
        assertThat(parseTreeCache, containsString("\"concurrencyLevel\":4"));
        String sqlStatementCache = String.valueOf(data.get(2));
        assertThat(sqlStatementCache, containsString("\"initialCapacity\":2000"));
        assertThat(sqlStatementCache, containsString("\"maximumSize\":65535"));
        assertThat(sqlStatementCache, containsString("\"concurrencyLevel\":4"));
    }

    private ShardingSphereRuleMetaData getGlobalRuleMetaData() {
        CacheOption parseTreeCache = new CacheOption(128, 1024, 4);
        CacheOption sqlStatementCache = new CacheOption(2000, 65535, 4);
        SQLParserRuleConfiguration sqlParserRuleConfiguration = new SQLParserRuleConfiguration(true, parseTreeCache, sqlStatementCache);
        return new ShardingSphereRuleMetaData(Collections.singleton(sqlParserRuleConfiguration), Collections.emptyList());
    }
}
