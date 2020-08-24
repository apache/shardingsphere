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

package org.apache.shardingsphere.kernel.context.runtime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.DatabaseMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CachedDatabaseMetaDataTest {
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Test
    public void assertGetRowIdLifetimeFromOriginMetaData() throws SQLException {
        RowIdLifetime rowIdLifetime = mock(RowIdLifetime.class);
        when(databaseMetaData.getRowIdLifetime()).thenReturn(rowIdLifetime);
        assertThat(new CachedDatabaseMetaData(databaseMetaData).getRowIdLifetime(), is(rowIdLifetime));
    }
    
    @Test
    public void assertGetRowIdLifetimeFromOriginMetaDataWhenNotSupported() throws SQLException {
        when(databaseMetaData.getRowIdLifetime()).thenThrow(SQLFeatureNotSupportedException.class);
        assertThat(new CachedDatabaseMetaData(databaseMetaData).getRowIdLifetime(), is(RowIdLifetime.ROWID_UNSUPPORTED));
    }
    
    @Test
    public void assertIsGeneratedKeyAlwaysReturned() throws SQLException {
        when(databaseMetaData.generatedKeyAlwaysReturned()).thenReturn(true);
        assertTrue(new CachedDatabaseMetaData(databaseMetaData).isGeneratedKeyAlwaysReturned());
    }
    
    @Test
    public void assertIsGeneratedKeyAlwaysReturnedWhenNotSupported() throws SQLException {
        when(databaseMetaData.generatedKeyAlwaysReturned()).thenThrow(AbstractMethodError.class);
        assertFalse(new CachedDatabaseMetaData(databaseMetaData).isGeneratedKeyAlwaysReturned());
    }
}
