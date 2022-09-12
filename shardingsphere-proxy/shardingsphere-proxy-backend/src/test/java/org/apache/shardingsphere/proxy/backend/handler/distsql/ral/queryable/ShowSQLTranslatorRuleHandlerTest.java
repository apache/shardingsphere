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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowSQLTranslatorRuleHandlerTest extends ProxyContextRestorer {
    
    @Test
    public void assertExecute() throws SQLException {
        ShowSQLTranslatorRuleHandler handler = new ShowSQLTranslatorRuleHandler();
        handler.init(new ShowSQLTranslatorRuleStatement(), null);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData metaData = createGlobalRuleMetaData("MYSQL", true);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(metaData);
        ProxyContext.init(contextManager);
        handler.execute();
        handler.next();
        List<Object> data = handler.getRowData().getData();
        assertThat(data.size(), is(2));
        assertThat(data.get(0), is("MYSQL"));
        assertThat(data.get(1), is("true"));
    }
    
    private ShardingSphereRuleMetaData createGlobalRuleMetaData(final String type, final boolean useOriginalSQLWhenTranslatingFailed) {
        SQLTranslatorRule rule = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration(type, useOriginalSQLWhenTranslatingFailed));
        return new ShardingSphereRuleMetaData(Collections.singleton(rule));
    }
}
