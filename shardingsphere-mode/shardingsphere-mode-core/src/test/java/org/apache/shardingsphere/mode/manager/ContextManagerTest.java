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

package org.apache.shardingsphere.mode.manager;

import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ContextManagerTest {
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    private ContextManager contextManager;
    
    @Before
    public void setUp() {
        contextManager = new ContextManager();
        contextManager.init(metaDataContexts, transactionContexts);
    }
    
    @SneakyThrows
    @Test
    public void assertClose() {
        contextManager.close();
        verify(metaDataContexts).close();
    }
    
    @Test
    public void assertRenewMetaDataContexts() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        contextManager.renewMetaDataContexts(metaDataContexts);
        assertThat(contextManager.getMetaDataContexts(), is(metaDataContexts));
    }
    
    @Test
    public void assertRenewTransactionContexts() {
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        contextManager.renewTransactionContexts(transactionContexts);
        assertThat(contextManager.getTransactionContexts(), is(transactionContexts));
    }
}
