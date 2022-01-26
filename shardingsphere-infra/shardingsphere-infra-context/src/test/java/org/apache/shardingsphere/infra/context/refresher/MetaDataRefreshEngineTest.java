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

package org.apache.shardingsphere.infra.context.refresher;

import lombok.SneakyThrows;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataRefreshEngineTest {
    
    @InjectMocks
    private MetaDataRefreshEngine metaDataRefreshEngine;
    
    @Test
    public void assertRefreshNonIgnorableSQLStatement() throws SQLException {
        final int dropTimes = 10;
        DropDatabaseStatement dropDatabaseStatement = mock(DropDatabaseStatement.class);
        for (int i = 0; i < dropTimes; i++) {
            metaDataRefreshEngine.refresh(dropDatabaseStatement, Collections::emptyList);
        }
        verify(dropDatabaseStatement, times(dropTimes)).getDatabaseName();
    }
    
    @Test
    public void assertRefreshIgnorableSQLStatement() throws SQLException {
        SelectStatement selectStatement = mock(SelectStatement.class);
        metaDataRefreshEngine.refresh(selectStatement, Collections::emptyList);
        assertTrue(getIgnorableSQLStatementClasses().contains(selectStatement.getClass()));
    }
    
    @SneakyThrows
    private Set<Class<? extends SQLStatement>> getIgnorableSQLStatementClasses() {
        Field field = MetaDataRefreshEngine.class.getDeclaredField("IGNORABLE_SQL_STATEMENT_CLASSES");
        field.setAccessible(true);
        return (Set<Class<? extends SQLStatement>>) field.get(null);
    }
}
