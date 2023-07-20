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

import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.oracle.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.quote.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProjectionUtilsTest {

    @Test
    void assertGetColumnLabelFromAlias() {
        IdentifierValue alias = null;
        DatabaseType databaseType = null;
        alias = new IdentifierValue("Data", QuoteCharacter.NONE);
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, databaseType), is(alias.getValue()));
        alias = new IdentifierValue("Data");
        databaseType = new PostgreSQLDatabaseType();
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, databaseType), is(alias.getValue().toLowerCase()));
        databaseType = new OpenGaussDatabaseType();
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, databaseType), is(alias.getValue().toLowerCase()));
        databaseType = new OracleDatabaseType();
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, databaseType), is(alias.getValue().toUpperCase()));
        databaseType = new MySQLDatabaseType();
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, databaseType), is(alias.getValue()));
    }

    @Test
    void assertGetColumnNameFromFunction() {
        String functionName = "Function";
        String functionExpression = "FunctionExpression";
        DatabaseType databaseType = new PostgreSQLDatabaseType();
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, databaseType), is(functionName.toLowerCase()));
        databaseType = new OpenGaussDatabaseType();
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, databaseType), is(functionName.toLowerCase()));
        databaseType = new OracleDatabaseType();
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, databaseType), is(functionExpression.replace(" ", "").toUpperCase()));
        databaseType = new MySQLDatabaseType();
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, databaseType), is(functionExpression));
    }
}
