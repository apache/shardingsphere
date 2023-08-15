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

package org.apache.shardingsphere.infra.binder.segment.select.projection.util;

import org.apache.shardingsphere.infra.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.oracle.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProjectionUtilsTest {
    
    private final IdentifierValue alias = new IdentifierValue("Data", QuoteCharacter.NONE);
    
    @Test
    void assertGetColumnLabelFromAlias() {
        assertThat(ProjectionUtils.getColumnLabelFromAlias(new IdentifierValue("Data", QuoteCharacter.QUOTE), new PostgreSQLDatabaseType()), is("Data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new PostgreSQLDatabaseType()), is("data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new OpenGaussDatabaseType()), is("data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new OracleDatabaseType()), is("DATA"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new MySQLDatabaseType()), is("Data"));
    }
    
    @Test
    void assertGetColumnNameFromFunction() {
        String functionName = "Function";
        String functionExpression = "FunctionExpression";
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new PostgreSQLDatabaseType()), is("function"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new OpenGaussDatabaseType()), is("function"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new OracleDatabaseType()), is("FUNCTIONEXPRESSION"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new MySQLDatabaseType()), is("FunctionExpression"));
    }
}
