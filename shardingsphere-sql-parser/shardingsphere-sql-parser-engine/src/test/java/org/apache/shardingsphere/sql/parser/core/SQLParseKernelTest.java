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

package org.apache.shardingsphere.sql.parser.core;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SQLParseKernelTest {
    
    private SQLParseKernel parseKernel;
    
    @Mock
    private SQLParserEngine parserEngine;
    
    @Before
    @SneakyThrows
    public void setUp() {
        NewInstanceServiceLoader.register(SQLParserEntry.class);
        parseKernel = new SQLParseKernel("MySQL", "SELECT 1");
        when(parserEngine.parse()).thenReturn(mock(ParserRuleContext.class));
        Field field = SQLParseKernel.class.getDeclaredField("parserEngine");
        field.setAccessible(true);
        field.set(parseKernel, parserEngine);
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertParse() {
        parseKernel.parse();
    }
}
