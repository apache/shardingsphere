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

package org.apache.shardingsphere.infra.merge.result.impl.decorator;

import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.fixture.TestDecoratorMergedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecoratorMergedResultTest {
    
    @Mock
    private MergedResult mergedResult;
    
    private TestDecoratorMergedResult decoratorMergedResult;
    
    @BeforeEach
    void setUp() {
        decoratorMergedResult = new TestDecoratorMergedResult(mergedResult);
    }
    
    @Test
    void assertGetValueWithColumnIndex() throws SQLException {
        when(mergedResult.getValue(1, Object.class)).thenReturn("1");
        assertThat(decoratorMergedResult.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test
    void assertGetCalenderValueWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(decoratorMergedResult.getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetInputStreamWithColumnIndex() throws SQLException {
        assertNull(decoratorMergedResult.getInputStream(1, "ascii"));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        when(mergedResult.wasNull()).thenReturn(true);
        assertTrue(decoratorMergedResult.wasNull());
    }
}
