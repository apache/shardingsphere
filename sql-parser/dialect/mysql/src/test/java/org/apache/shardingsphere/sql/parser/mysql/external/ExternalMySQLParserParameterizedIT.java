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

package org.apache.shardingsphere.sql.parser.mysql.external;

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
public final class ExternalMySQLParserParameterizedIT extends ExternalSQLParserParameterizedIT {
    
    public ExternalMySQLParserParameterizedIT(final ExternalSQLParserParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ExternalSQLParserParameterizedArray> getTestParameters() {
        String caseURL = "https://github.com/mysql/mysql-server/tree/8.0/mysql-test/t";
        String resultURL = "https://github.com/mysql/mysql-server/tree/8.0/mysql-test/r";
        return new SQLCaseLoader(new GitHubSQLCaseLoadStrategy()).load(URI.create(caseURL), URI.create(resultURL), DatabaseTypeFactory.getInstance("MySQL"), "CSV");
    }
}
