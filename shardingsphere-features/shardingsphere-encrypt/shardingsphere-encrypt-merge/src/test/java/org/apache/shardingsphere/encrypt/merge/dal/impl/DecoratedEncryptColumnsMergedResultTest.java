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

package org.apache.shardingsphere.encrypt.merge.dal.impl;

import org.apache.shardingsphere.encrypt.merge.dal.impl.fixture.TestStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.model.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DecoratedEncryptColumnsMergedResultTest {
    
    @Test
    public void assertNewValidResult() throws SQLException {
        TestStatementContext testStatementContext = mock(TestStatementContext.class);
        SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        IdentifierValue identifierValue = mock(IdentifierValue.class);
        when(identifierValue.getValue()).thenReturn("value");
        TableNameSegment tableNameSegment = new TableNameSegment(0, 1, identifierValue);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(testStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.next()).thenReturn(true);
        when(mergedResult.wasNull()).thenReturn(false);
        when(mergedResult.getValue(eq(new Integer(1)), eq(getClass()))).thenReturn("test");
        DecoratedEncryptColumnsMergedResult actual = new DecoratedEncryptColumnsMergedResult(mergedResult, testStatementContext, mock(SchemaMetaData.class));
        assertNotNull(actual);
        assertTrue(actual.nextValue());
        assertFalse(actual.wasNull());
        assertThat(actual.getOriginalValue(1, getClass()), is("test"));
    }
}
