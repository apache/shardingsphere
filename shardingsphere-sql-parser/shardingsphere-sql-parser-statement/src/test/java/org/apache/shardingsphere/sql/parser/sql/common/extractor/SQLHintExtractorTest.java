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

package org.apache.shardingsphere.sql.parser.sql.common.extractor;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLHintExtractorTest {
    
    @Test
    public void assertFindHintDataSourceNameExist() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* shardingsphere hint: datasourceName=ds_1 */", 0, 0)));
        Optional<String> dataSourceName = SQLHintExtractor.findHintDataSourceName(statement);
        assertTrue(dataSourceName.isPresent());
        assertThat(dataSourceName.get(), is("ds_1"));
    }
    
    @Test
    public void assertFindHintDataSourceNameNotExist() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* no hint */", 0, 0)));
        Optional<String> dataSourceName = SQLHintExtractor.findHintDataSourceName(statement);
        assertFalse(dataSourceName.isPresent());
    }
    
    @Test
    public void assertFindHintDataSourceNameNotExistWithoutComment() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        Optional<String> dataSourceName = SQLHintExtractor.findHintDataSourceName(statement);
        assertFalse(dataSourceName.isPresent());
    }
}
