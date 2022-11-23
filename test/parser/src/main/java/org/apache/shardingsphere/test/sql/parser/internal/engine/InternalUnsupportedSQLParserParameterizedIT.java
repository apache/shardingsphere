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

package org.apache.shardingsphere.test.sql.parser.internal.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.parser.internal.cases.sql.domain.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.internal.cases.sql.registry.UnsupportedSQLCasesRegistry;
import org.apache.shardingsphere.test.sql.parser.internal.loader.SQLCasesLoader;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

@RequiredArgsConstructor
public abstract class InternalUnsupportedSQLParserParameterizedIT {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = UnsupportedSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    protected static Collection<Object[]> getTestParameters(final String databaseType) {
        return SQL_CASES_LOADER.getTestParameters(Collections.singleton(databaseType));
    }
    
    @Test(expected = Exception.class)
    // TODO should expect SQLParsingException only
    public final void assertUnsupportedSQL() {
        String sql = SQL_CASES_LOADER.getCaseValue(sqlCaseId, sqlCaseType, Collections.emptyList(), databaseType);
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        CacheOption cacheOption = new CacheOption(128, 1024L);
        ParseASTNode parseContext = new SQLParserEngine(databaseType, cacheOption).parse(sql, false);
        // TODO remove SQLStatement sqlStatement =
        SQLStatement sqlStatement = new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(parseContext);
    }
}
