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

package org.apache.shardingsphere.distsql.handler.util;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.available.FromDatabaseAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseNameUtilsTest {
    
    private static final String CURRENT_DATABASE = "CURRENT_DATABASE";
    
    private static final String AVAILABLE_DATABASE = "AVAILABLE_DATABASE";
    
    @Mock
    private SQLStatement sqlStatement;
    
    private FromDatabaseAvailable fromDatabaseAvailable = mock(FromDatabaseAvailable.class, withSettings().extraInterfaces(SQLStatement.class));
    
    @Mock
    private DatabaseSegment databaseSegment;
    
    @Mock
    private IdentifierValue identifierValue;
    
    @BeforeEach
    void setup() {
        when(fromDatabaseAvailable.getDatabase()).thenReturn(Optional.of(databaseSegment));
        when(databaseSegment.getIdentifier()).thenReturn(identifierValue);
        when(identifierValue.getValue()).thenReturn(AVAILABLE_DATABASE);
    }
    
    @Test
    void assertDatabaseNameWhenAvailableInSqlStatement() {
        assertThat(DatabaseNameUtils.getDatabaseName((SQLStatement) fromDatabaseAvailable, CURRENT_DATABASE), is(AVAILABLE_DATABASE));
    }
    
    @Test
    void assertDatabaseNameWhenNotAvailableInSqlStatement() {
        assertThat(DatabaseNameUtils.getDatabaseName(sqlStatement, CURRENT_DATABASE), is(CURRENT_DATABASE));
    }
}
