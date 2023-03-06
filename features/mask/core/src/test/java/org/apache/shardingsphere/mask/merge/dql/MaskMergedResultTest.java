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

package org.apache.shardingsphere.mask.merge.dql;

import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
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
public final class MaskMergedResultTest {
    
    @Mock
    private MaskAlgorithmMetaData metaData;
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    public void assertNext() throws SQLException {
        assertFalse(new MaskMergedResult(metaData, mergedResult).next());
    }
    
    @Test
    public void assertGetValue() throws SQLException {
        when(mergedResult.getValue(1, Object.class)).thenReturn("VALUE");
        MaskAlgorithm<String, String> maskAlgorithm = mock(MaskAlgorithm.class);
        when(maskAlgorithm.mask("VALUE")).thenReturn("MASK_VALUE");
        when(metaData.findMaskAlgorithmByColumnIndex(1)).thenReturn(Optional.of(maskAlgorithm));
        assertThat(new MaskMergedResult(metaData, mergedResult).getValue(1, String.class), is("MASK_VALUE"));
    }
    
    @Test
    public void assertGetCalendarValue() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(new MaskMergedResult(metaData, mergedResult).getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetInputStream() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergedResult.getInputStream(1, "asc")).thenReturn(inputStream);
        assertThat(new MaskMergedResult(metaData, mergedResult).getInputStream(1, "asc"), is(inputStream));
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        assertFalse(new MaskMergedResult(metaData, mergedResult).wasNull());
    }
}
