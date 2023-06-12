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

import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptMergedResultTest {
    
    @Mock
    private EncryptAlgorithmMetaData metaData;
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    void assertNext() throws SQLException {
        assertFalse(new EncryptMergedResult(metaData, mergedResult).next());
    }
    
    @Test
    void assertGetValueWithoutEncryptContext() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("VALUE");
        when(metaData.findEncryptContext(1)).thenReturn(Optional.empty());
        assertThat(new EncryptMergedResult(metaData, mergedResult).getValue(1, String.class), is("VALUE"));
    }
    
    @Test
    void assertGetCalendarValue() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(new EncryptMergedResult(metaData, mergedResult).getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetInputStream() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergedResult.getInputStream(1, "asc")).thenReturn(inputStream);
        assertThat(new EncryptMergedResult(metaData, mergedResult).getInputStream(1, "asc"), is(inputStream));
    }
    
    @Test
    void assertGetCharacterStream() throws SQLException {
        Reader reader = mock(Reader.class);
        when(mergedResult.getCharacterStream(1)).thenReturn(reader);
        assertThat(new EncryptMergedResult(metaData, mergedResult).getCharacterStream(1), is(reader));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(new EncryptMergedResult(metaData, mergedResult).wasNull());
    }
}
