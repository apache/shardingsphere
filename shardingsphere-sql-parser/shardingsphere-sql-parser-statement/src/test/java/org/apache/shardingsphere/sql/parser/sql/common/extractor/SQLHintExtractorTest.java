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

public class SQLHintExtractorTest {
    
    @Test
    public void assertFindHintDatasourceNameExist() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* shardingsphere hint: datasourceName=ds-1 */", 0, 0)));
        Optional<String> datasourceName = SQLHintExtractor.findHintDatasourceName(statement);
        assertTrue(datasourceName.isPresent());
        assertThat(datasourceName.get(), is("ds-1"));
    }
    
    @Test
    public void assertFindHintDatasourceNameNotExist() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        when(statement.getCommentSegments()).thenReturn(Collections.singletonList(new CommentSegment("/* no hint */", 0, 0)));
        Optional<String> datasourceName = SQLHintExtractor.findHintDatasourceName(statement);
        assertFalse(datasourceName.isPresent());
    }
    
    @Test
    public void assertFindHintDatasourceNameNotExistWithoutComment() {
        AbstractSQLStatement statement = mock(AbstractSQLStatement.class);
        Optional<String> datasourceName = SQLHintExtractor.findHintDatasourceName(statement);
        assertFalse(datasourceName.isPresent());
    }
}
