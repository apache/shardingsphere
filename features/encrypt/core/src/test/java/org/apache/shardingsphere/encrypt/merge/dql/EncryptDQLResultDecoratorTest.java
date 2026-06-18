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

package org.apache.shardingsphere.encrypt.merge.dql;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptDQLResultDecoratorTest {
    
    @Test
    void assertDecorate() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.next()).thenReturn(true);
        EncryptRule rule = mock(EncryptRule.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        EncryptDQLResultDecorator decorator =
                new EncryptDQLResultDecorator(database, mock(ShardingSphereMetaData.class), mock(SelectStatementContext.class, RETURNS_DEEP_STUBS));
        MergedResult actual = decorator.decorate(mergedResult, mock());
        assertTrue(actual.next());
    }
}
