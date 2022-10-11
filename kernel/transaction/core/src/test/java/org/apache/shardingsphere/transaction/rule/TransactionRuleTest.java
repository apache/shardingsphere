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

package org.apache.shardingsphere.transaction.rule;

import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TransactionRuleTest {
    
    @Test(expected = IllegalStateException.class)
    public void assertMultiDatabaseTypeFail() {
        TransactionRuleConfiguration transactionRuleConfiguration = mock(TransactionRuleConfiguration.class);
        when(transactionRuleConfiguration.getDefaultType()).thenReturn("XA");
        when(transactionRuleConfiguration.getProviderType()).thenReturn("Atomikos");
        ShardingSphereDatabase db1 = mock(ShardingSphereDatabase.class);
        ShardingSphereResourceMetaData resources1 = mock(ShardingSphereResourceMetaData.class);
        when(resources1.getDatabaseType()).thenReturn(new OpenGaussDatabaseType());
        when(db1.getResourceMetaData()).thenReturn(resources1);
        ShardingSphereDatabase db2 = mock(ShardingSphereDatabase.class);
        ShardingSphereResourceMetaData resources2 = mock(ShardingSphereResourceMetaData.class);
        when(resources2.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        when(db2.getResourceMetaData()).thenReturn(resources2);
        Map<String, ShardingSphereDatabase> databaseMap = new HashMap<>();
        databaseMap.put("db1", db1);
        databaseMap.put("db2", db2);
        TransactionRule transactionRule = new TransactionRule(transactionRuleConfiguration, databaseMap, null);
    }
}
