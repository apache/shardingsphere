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

package org.apache.shardingsphere.test.integration.sql.parser.engine.dialect;

import org.apache.shardingsphere.test.integration.sql.parser.engine.SQLParserParameterizedIT;
import org.apache.shardingsphere.test.integration.sql.parser.loader.SQLCaseLoader;
import org.apache.shardingsphere.test.integration.sql.parser.loader.impl.GitHubSQLCaseLoadStrategy;
import org.apache.shardingsphere.test.integration.sql.parser.result.SQLParserCSVResultProcessor;
import org.apache.shardingsphere.test.runner.ShardingSphereParallelTestParameterized;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.net.URI;
import java.util.Collection;

@RunWith(ShardingSphereParallelTestParameterized.class)
public final class PostgreSQLParserParameterizedIT extends SQLParserParameterizedIT {
    
    public PostgreSQLParserParameterizedIT(final String sqlCaseId, final String sqlCaseValue) {
        super(sqlCaseId, sqlCaseValue, "PostgreSQL", new SQLParserCSVResultProcessor("PostgreSQL"));
    }
    
    @Parameters(name = "{0} (PostgreSQL) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return new SQLCaseLoader(new GitHubSQLCaseLoadStrategy()).load(
                URI.create("https://github.com/postgres/postgres/tree/master/src/test/regress/sql"), URI.create("https://github.com/postgres/postgres/tree/master/src/test/regress/expected"));
    }
}
