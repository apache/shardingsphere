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

package org.apache.shardingsphere.underlying.common.log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataLoggerTest {
    
    @Mock
    private Logger log;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field field = MetaDataLogger.class.getDeclaredField("log");
        setFinalStatic(field, log);
    }
    
    @Test
    public void assertLog() {
        MetaDataLogger.log("Load all tables meta data.");
        InOrder inOrder = inOrder(log);
        inOrder.verify(log).info("Load all tables meta data.", new Object[]{});
    }
    
    @Test
    public void assertLogTableMetaData() {
        MetaDataLogger.logTableMetaData("test", "schema", "table");
        InOrder inOrder = inOrder(log);
        inOrder.verify(log).info("Loading table meta data catalog: {}, schema: {}, table: {}.", "test", "schema", "table");
    }
    
    @Test
    public void assertLogTableMetaDataWithNullSchema() {
        MetaDataLogger.logTableMetaData("test", "table");
        InOrder inOrder = inOrder(log);
        inOrder.verify(log).info("Loading table meta data catalog: {}, table: {}.", new Object[]{"test", "table"});
    }
    
    private static void setFinalStatic(final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
