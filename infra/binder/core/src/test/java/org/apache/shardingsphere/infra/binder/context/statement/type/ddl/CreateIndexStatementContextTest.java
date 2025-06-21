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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateIndexStatementContextTest {
    
    @Test
    void assertNewInstance() {
        CreateIndexStatement createIndexStatement = mock(CreateIndexStatement.class);
        CreateIndexStatementContext actual = new CreateIndexStatementContext(mock(), createIndexStatement);
        assertThat(actual.getSqlStatement(), is(createIndexStatement));
        assertTrue(actual.isGeneratedIndex());
        assertThat(actual.getTablesContext().getSimpleTables(), is(Collections.emptyList()));
        assertThat(actual.getIndexes(), is(Collections.emptyList()));
        when(createIndexStatement.getIndex()).thenReturn(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("index_2"))));
        CreateIndexStatementContext actual2 = new CreateIndexStatementContext(mock(), createIndexStatement);
        assertThat(actual2.getIndexes().stream().map(each -> each.getIndexName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("index_2")));
    }
}
