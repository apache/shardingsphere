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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.FlushStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseDeleteOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseInsertOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseRegionReloadOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseSelectOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.type.HBaseUpdateOperationConverter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class HBaseOperationConverterFactoryTest {
    
    @Test
    void assertExecuteSelectStatement() {
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        assertThat(converter, instanceOf(HBaseSelectOperationConverter.class));
    }
    
    @Test
    void assertExecuteInsertStatement() {
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        assertThat(converter, instanceOf(HBaseInsertOperationConverter.class));
    }
    
    @Test
    void assertExecuteUpdateStatement() {
        SQLStatementContext sqlStatementContext = mock(UpdateStatementContext.class);
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        assertThat(converter, instanceOf(HBaseUpdateOperationConverter.class));
    }
    
    @Test
    void assertExecuteDeleteStatement() {
        SQLStatementContext sqlStatementContext = mock(DeleteStatementContext.class);
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        assertThat(converter, instanceOf(HBaseDeleteOperationConverter.class));
    }
    
    @Test
    void assertExecuteFlushStatement() {
        SQLStatementContext sqlStatementContext = mock(FlushStatementContext.class);
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        assertThat(converter, instanceOf(HBaseRegionReloadOperationConverter.class));
    }
}
