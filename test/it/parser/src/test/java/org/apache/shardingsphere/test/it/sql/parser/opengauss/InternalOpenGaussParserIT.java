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

package org.apache.shardingsphere.test.it.sql.parser.opengauss;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserIT;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserITSettings;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@InternalSQLParserITSettings("openGauss")
class InternalOpenGaussParserIT extends InternalSQLParserIT {
    
    @Test
    void assertSelectWithLargeInList() {
        Collection<String> expectedValues = IntStream.rangeClosed(1, 100000).mapToObj(String::valueOf).collect(Collectors.toList());
        String sql = "SELECT * FROM foo_table WHERE foo_id IN (" + String.join(", ", expectedValues) + ")";
        SelectStatement actual = (SelectStatement) new SQLStatementVisitorEngine("openGauss").visit(new SQLParserEngine("openGauss", new CacheOption(128, 1024L)).parse(sql, false));
        ListExpression actualValues = (ListExpression) ((InExpression) actual.getWhere().get().getExpr()).getRight();
        assertThat(actualValues.getItems().stream().map(each -> sql.substring(each.getStartIndex(), each.getStopIndex() + 1)).collect(Collectors.toList()), is(expectedValues));
    }
}
