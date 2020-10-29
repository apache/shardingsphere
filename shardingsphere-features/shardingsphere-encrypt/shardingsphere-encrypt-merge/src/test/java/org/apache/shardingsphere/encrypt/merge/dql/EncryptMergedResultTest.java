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

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptMergedResultTest {
    
    @Mock
    private EncryptAlgorithmMetaData metaData;
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    public void assertNext() throws SQLException {
        assertFalse(new EncryptMergedResult(metaData, mergedResult, true).next());
    }
    
    @Test
    public void assertGetValueWithQueryWithPlainColumn() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("VALUE");
        assertThat(new EncryptMergedResult(metaData, mergedResult, false).getValue(1, String.class), is("VALUE"));
    }
    
    @Test
    public void assertGetValueWithQueryWithCipherColumnAndMismatchedEncryptor() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("VALUE");
        when(metaData.findEncryptor(1)).thenReturn(Optional.empty());
        assertThat(new EncryptMergedResult(metaData, mergedResult, true).getValue(1, String.class), is("VALUE"));
    }
    
    @Test
    public void assertGetValueWithQueryWithCipherColumnAndMatchedEncryptorWithNotNullCiphertext() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("VALUE");
        EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        when(encryptAlgorithm.decrypt("VALUE")).thenReturn("ORIGINAL_VALUE");
        when(metaData.findEncryptor(1)).thenReturn(Optional.of(encryptAlgorithm));
        assertThat(new EncryptMergedResult(metaData, mergedResult, true).getValue(1, String.class), is("ORIGINAL_VALUE"));
    }
    
    @Test
    public void assertGetValueWithQueryWithCipherColumnAndMatchedEncryptorWithNullCiphertext() throws SQLException {
        EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        when(metaData.findEncryptor(1)).thenReturn(Optional.of(encryptAlgorithm));
        assertNull(new EncryptMergedResult(metaData, mergedResult, true).getValue(1, String.class));
    }
    
    @Test
    public void assertGetCalendarValue() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(new EncryptMergedResult(metaData, mergedResult, true).getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetInputStream() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergedResult.getInputStream(1, "asc")).thenReturn(inputStream);
        assertThat(new EncryptMergedResult(metaData, mergedResult, true).getInputStream(1, "asc"), is(inputStream));
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        assertFalse(new EncryptMergedResult(metaData, mergedResult, true).wasNull());
    }
}
