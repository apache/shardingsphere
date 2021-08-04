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

package org.apache.shardingsphere.proxy.backend.text.database;

import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDatabaseBackendHandlerTest {

    @Mock
    private TransactionContexts transactionContexts;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;

    @Mock
    private CreateDatabaseStatement statement;

    private CreateDatabaseBackendHandler handler;

    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        handler = new CreateDatabaseBackendHandler(statement);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test_db"));
    }

    @Test
    public void assertExecuteCreateNewDatabase() {
        when(statement.getDatabaseName()).thenReturn("other_db");
        ResponseHeader responseHeader = handler.execute();
        Assert.assertTrue(responseHeader instanceof UpdateResponseHeader);
    }

    @Test(expected = DBCreateExistsException.class)
    public void assertExecuteCreateExistDatabase() {
        when(statement.getDatabaseName()).thenReturn("test_db");
        ResponseHeader responseHeader = handler.execute();
        Assert.assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
}
