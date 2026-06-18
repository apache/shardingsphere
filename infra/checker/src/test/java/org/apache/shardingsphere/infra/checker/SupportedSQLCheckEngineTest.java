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

package org.apache.shardingsphere.infra.checker;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(OrderedSPILoader.class)
class SupportedSQLCheckEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckSQL() {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        SupportedSQLChecker supportedSQLChecker = mock(SupportedSQLChecker.class);
        when(supportedSQLChecker.isCheck(sqlStatementContext)).thenReturn(true);
        SupportedSQLChecker unsupportedSQLChecker = mock(SupportedSQLChecker.class);
        SupportedSQLCheckersBuilder supportedSQLCheckersBuilder = mock(SupportedSQLCheckersBuilder.class);
        when(supportedSQLCheckersBuilder.getSupportedSQLCheckers()).thenReturn(Arrays.asList(supportedSQLChecker, unsupportedSQLChecker));
        Map<ShardingSphereRule, SupportedSQLCheckersBuilder> services = Collections.singletonMap(rule, supportedSQLCheckersBuilder);
        when(OrderedSPILoader.getServices(SupportedSQLCheckersBuilder.class, Collections.singleton(rule))).thenReturn(services);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        new SupportedSQLCheckEngine().checkSQL(Collections.singleton(rule), sqlStatementContext, database);
        verify(supportedSQLChecker).check(eq(rule), eq(database), any(), eq(sqlStatementContext));
        verify(unsupportedSQLChecker, never()).check(eq(rule), eq(database), any(), eq(sqlStatementContext));
    }
}
