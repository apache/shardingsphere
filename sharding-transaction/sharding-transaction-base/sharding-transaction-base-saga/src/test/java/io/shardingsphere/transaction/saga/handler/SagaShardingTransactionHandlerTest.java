/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.saga.handler;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.saga.manager.SagaResourceManager;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

@RunWith(MockitoJUnitRunner.class)
public class SagaShardingTransactionHandlerTest {
    
    private final SagaShardingTransactionHandler handler = new SagaShardingTransactionHandler();
    
    @Mock
    private SagaTransactionManager sagaTransactionManager;
    
    @Mock
    private SagaResourceManager sagaResourceManager;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, SQLException {
        Field transactionManagerField = SagaShardingTransactionHandler.class.getDeclaredField("transactionManager");
        transactionManagerField.setAccessible(true);
        transactionManagerField.set(handler, sagaTransactionManager);
        when(sagaTransactionManager.getResourceManager()).thenReturn(sagaResourceManager);
    }
    
    @Test
    public void assertGetTransactionType() {
        assertThat(handler.getTransactionType(), equalTo(TransactionType.BASE));
    }
    
    @Test
    public void assertGetTransactionManager() {
        assertThat(handler.getShardingTransactionManager(), CoreMatchers.<ShardingTransactionManager>equalTo(sagaTransactionManager));
    }
    
    @Test
    public void assertRegisterTransactionalResource() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        handler.registerTransactionalResource(DatabaseType.MySQL, dataSourceMap);
        verify(sagaResourceManager).registerDataSourceMap(dataSourceMap);
    }
    
    @Test
    public void assertClearTransactionalResource() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        handler.clearTransactionalResource(dataSourceMap);
        verify(sagaResourceManager).releaseDataSourceMap(dataSourceMap);
    }
}
