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

import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.fixture.LexerFixture;
import org.apache.shardingsphere.sql.parser.fixture.ParserFixture;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

public final class SQLParserFactoryTest {
    
    private static final String SQL = "SELECT COUNT(*) FROM user";
    
    @Test
    public void newInstance() {
        SQLLexer sqlLexer = mock(LexerFixture.class);
        SQLParser sqlParser = mock(ParserFixture.class);
        SQLParser result = SQLParserFactory.newInstance(SQL, sqlLexer.getClass(), sqlParser.getClass());
        assertThat(result, instanceOf(ParserFixture.class));
    }
}
