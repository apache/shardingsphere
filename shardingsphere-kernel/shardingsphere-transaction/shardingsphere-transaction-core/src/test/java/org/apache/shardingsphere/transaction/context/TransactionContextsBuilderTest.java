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

package org.apache.shardingsphere.transaction.context;

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TransactionContextsBuilderTest {
    
    @Test
    public void assertNewInstanceWithEmptyEngines() {
        TransactionContexts transactionContexts = new TransactionContextsBuilder(Collections.emptyMap(), Collections.emptyList()).build();
        assertTrue(transactionContexts.getEngines().isEmpty());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetDefaultTransactionManagerEngine() {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(1, 1);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(metaData.getResource().getDataSources()).thenReturn(createDataSourceMap());
        metaDataMap.put(DefaultSchema.LOGIC_NAME, metaData);
        Collection<ShardingSphereRule> globalRules = new LinkedList<>();
        globalRules.add(new TransactionRule(new TransactionRuleConfiguration(TransactionType.LOCAL.name(), null)));
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataMap, globalRules).build();
        Map<String, ShardingSphereTransactionManagerEngine> engines = transactionContexts.getEngines();
        assertThat(engines.size(), is(1));
        assertNotNull(transactionContexts.getEngines().get(DefaultSchema.LOGIC_NAME));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class));
        result.put("ds_1", mock(DataSource.class));
        return result;
    }
}
