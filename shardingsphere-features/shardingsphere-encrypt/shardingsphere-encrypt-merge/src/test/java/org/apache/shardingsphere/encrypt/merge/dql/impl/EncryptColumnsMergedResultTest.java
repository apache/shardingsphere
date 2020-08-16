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

package org.apache.shardingsphere.encrypt.merge.dql.impl;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.merge.dql.fixture.EncryptColumnsMergedResultFixture;
import org.apache.shardingsphere.encrypt.merge.dql.fixture.TableAvailableAndSqlStatementContextFixture;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptColumnsMergedResultTest {
    
    @Mock
    private TableAvailableAndSqlStatementContextFixture tableAvailableAndSqlStatementContextFixture;
    
    @Mock
    private SchemaMetaData schemaMetaData;

    @Mock
    private SimpleTableSegment simpleTableSegment;
    
    @Mock
    private TableNameSegment tableNameSegment;
    
    @Mock
    private IdentifierValue identifierValue;
    
    @Mock
    private EncryptColumnsMergedResultFixture encryptColumnsMergedResultFixture;
    
    @Before
    public void setUp() {
        when(tableAvailableAndSqlStatementContextFixture.getAllTables()).thenReturn(Lists.newArrayList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(identifierValue.getValue()).thenReturn("t_order");
        encryptColumnsMergedResultFixture = spy(new EncryptColumnsMergedResultFixture(tableAvailableAndSqlStatementContextFixture, schemaMetaData));
    }
    
    @Test
    @SneakyThrows
    public void assertNextWithEmptyColumnMetaData() {
        when(encryptColumnsMergedResultFixture.nextValue()).thenReturn(true);
        System.out.println(encryptColumnsMergedResultFixture.next());
    }
}
