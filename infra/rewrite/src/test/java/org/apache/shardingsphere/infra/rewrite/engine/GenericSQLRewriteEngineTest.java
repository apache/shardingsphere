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

package org.apache.shardingsphere.infra.rewrite.engine;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class GenericSQLRewriteEngineTest {
    
    @Test
    void assertRewrite() {
        DatabaseType databaseType = mock(DatabaseType.class);
        SQLTranslatorRule rule = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration());
        GenericSQLRewriteResult actual = new GenericSQLRewriteEngine(rule, databaseType, Collections.singletonMap("ds_0", databaseType)).rewrite(new SQLRewriteContext(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap("test", mock(ShardingSphereSchema.class)), mock(CommonSQLStatementContext.class), "SELECT 1", Collections.emptyList(), mock(ConnectionContext.class),
                new HintValueContext()));
        assertThat(actual.getSqlRewriteUnit().getSql(), is("SELECT 1"));
        assertThat(actual.getSqlRewriteUnit().getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    void assertRewriteStorageTypeIsEmpty() {
        SQLTranslatorRule rule = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration());
        GenericSQLRewriteResult actual = new GenericSQLRewriteEngine(rule, mock(DatabaseType.class), Collections.emptyMap()).rewrite(new SQLRewriteContext(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap("test", mock(ShardingSphereSchema.class)), mock(CommonSQLStatementContext.class), "SELECT 1", Collections.emptyList(), mock(ConnectionContext.class),
                new HintValueContext()));
        assertThat(actual.getSqlRewriteUnit().getSql(), is("SELECT 1"));
        assertThat(actual.getSqlRewriteUnit().getParameters(), is(Collections.emptyList()));
    }
}
