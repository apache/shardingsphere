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

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.util.ProjectionUtils;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.oracle.type.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProjectionUtilsTest {
    
    @Test
    void assertGetColumnLabelFromAlias() {
        IdentifierValue alias = new IdentifierValue("Data", QuoteCharacter.NONE);
        assertThat(ProjectionUtils.getColumnLabelFromAlias(new IdentifierValue("Data", QuoteCharacter.QUOTE), new PostgreSQLDatabaseType()), is("Data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new PostgreSQLDatabaseType()), is("data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new OpenGaussDatabaseType()), is("data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new OracleDatabaseType()), is("DATA"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, new MySQLDatabaseType()), is("Data"));
    }
    
    @Test
    void assertGetColumnNameFromFunction() {
        String functionName = "Cast";
        String functionExpression = "Cast(order_id AS INT)";
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new PostgreSQLDatabaseType()), is("cast"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new OpenGaussDatabaseType()), is("cast"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new OracleDatabaseType()), is("CAST(ORDER_IDASINT)"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, new MySQLDatabaseType()), is("Cast(order_id AS INT)"));
    }
}
