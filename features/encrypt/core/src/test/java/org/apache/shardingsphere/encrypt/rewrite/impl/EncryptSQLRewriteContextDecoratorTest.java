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

package org.apache.shardingsphere.encrypt.rewrite.impl;

import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptSQLRewriteContextDecoratorTest {
    
    private final EncryptSQLRewriteContextDecorator encryptSQLRewriteContextDecorator = new EncryptSQLRewriteContextDecorator();
    
    @Test
    public void assertDecorate() {
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        CommonSQLStatementContext sqlStatementContext = mock(CommonSQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlRewriteContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        encryptSQLRewriteContextDecorator.decorate(mock(EncryptRule.class), mock(ConfigurationProperties.class), sqlRewriteContext, mock(RouteContext.class));
        assertTrue(sqlRewriteContext.getSqlTokens().isEmpty());
    }
}
