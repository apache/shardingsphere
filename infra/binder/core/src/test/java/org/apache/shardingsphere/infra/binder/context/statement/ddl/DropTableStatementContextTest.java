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

package org.apache.shardingsphere.infra.binder.context.statement.ddl;

import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.sql92.ddl.SQL92DropTableStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DropTableStatementContextTest {
    
    @Test
    void assertNewInstance() {
        DropTableStatement dropTableStatement = new SQL92DropTableStatement();
        DropTableStatementContext actual = new DropTableStatementContext(dropTableStatement);
        dropTableStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        dropTableStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_tbl"))));
        assertThat(actual.getSqlStatement(), is(dropTableStatement));
    }
}
