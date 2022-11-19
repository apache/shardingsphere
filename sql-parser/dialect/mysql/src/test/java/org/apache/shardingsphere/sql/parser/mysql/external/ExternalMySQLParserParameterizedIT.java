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

import org.apache.shardingsphere.test.sql.parser.external.engine.ExternalSQLParserParameterizedIT;
import org.apache.shardingsphere.test.sql.parser.external.loader.SQLCaseLoader;
import org.apache.shardingsphere.test.sql.parser.external.loader.strategy.impl.GitHubSQLCaseLoadStrategy;
import org.apache.shardingsphere.test.runner.ShardingSphereParallelTestParameterized;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.net.URI;
import java.util.Collection;

@RunWith(ShardingSphereParallelTestParameterized.class)
public final class ExternalMySQLParserParameterizedIT extends ExternalSQLParserParameterizedIT {
    
    public ExternalMySQLParserParameterizedIT(final String sqlCaseId, final String sql) {
        super(sqlCaseId, sql, "MySQL", "CSV");
    }
    
    @Parameters(name = "{0} (MySQL) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return new SQLCaseLoader(new GitHubSQLCaseLoadStrategy()).load(
                URI.create("https://github.com/mysql/mysql-server/tree/8.0/mysql-test/t"), URI.create("https://github.com/mysql/mysql-server/tree/8.0/mysql-test/r"));
    }
}
