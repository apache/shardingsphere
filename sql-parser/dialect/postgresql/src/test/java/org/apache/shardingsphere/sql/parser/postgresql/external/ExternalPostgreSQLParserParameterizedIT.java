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

package org.apache.shardingsphere.sql.parser.postgresql.external;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.test.sql.parser.external.engine.ExternalSQLParserParameterizedIT;
import org.apache.shardingsphere.test.sql.parser.external.engine.param.ExternalSQLParserParameterizedArray;
import org.apache.shardingsphere.test.sql.parser.external.loader.SQLCaseLoader;
import org.apache.shardingsphere.test.sql.parser.external.loader.strategy.impl.GitHubSQLCaseLoadStrategy;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.net.URI;
import java.util.Collection;

@RunWith(Parameterized.class)
public final class ExternalPostgreSQLParserParameterizedIT extends ExternalSQLParserParameterizedIT {
    
    public ExternalPostgreSQLParserParameterizedIT(final ExternalSQLParserParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Parameters(name = "{0} (PostgreSQL) -> {1}")
    public static Collection<ExternalSQLParserParameterizedArray> getTestParameters() {
        String caseURL = "https://github.com/postgres/postgres/tree/master/src/test/regress/sql";
        String resultURL = "https://github.com/postgres/postgres/tree/master/src/test/regress/expected";
        return new SQLCaseLoader(new GitHubSQLCaseLoadStrategy()).load(URI.create(caseURL), URI.create(resultURL), DatabaseTypeFactory.getInstance("PostgreSQL"), "CSV");
    }
}
