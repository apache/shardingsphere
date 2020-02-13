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

import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class MetaDataLoggerTest {
    
    @Mock
    private Logger log;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field field = MetaDataLogger.class.getDeclaredField("log");
        setFinalStatic(field, log);
    }
    
    @Test
    public void assertLogTableMetaData() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(1);
        TableMetaData tableMetaData = new TableMetaData(Collections.<ColumnMetaData>emptyList(), Collections.<String>emptyList());
        tableMetaDataMap.put("test", tableMetaData);
        MetaDataLogger.logTableMetaData(tableMetaDataMap);
        InOrder inOrder = inOrder(log);
        inOrder.verify(log).info("load all tables MetaData.", new Object[]{});
        inOrder.verify(log).info("tableName:{}, tableInfo:{}", new Object[]{"test", tableMetaData});
        inOrder.verify(log).info("load {} tables.", new Object[]{tableMetaDataMap.keySet().size()});
    }
    
    private static void setFinalStatic(final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
    
}
