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

package org.apache.shardingsphere.test.sql.parser.parameterized.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.engine.SQLStatementParserEngineFactory;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.loader.UnsupportedSQLCasesRegistry;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public abstract class UnsupportedSQLParserParameterizedTest {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = UnsupportedSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    protected static Collection<Object[]> getTestParameters(final String databaseType) {
        return SQL_CASES_LOADER.getSQLTestParameters(Collections.singleton(databaseType));
    }
    
    @Test(expected = SQLParsingException.class)
    public final void assertUnsupportedSQL() {
        String sql = SQL_CASES_LOADER.getSQL(sqlCaseId, sqlCaseType, Collections.emptyList());
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        SQLStatementParserEngineFactory.getSQLParserEngine(databaseType).parse(sql, false);
    }
}
