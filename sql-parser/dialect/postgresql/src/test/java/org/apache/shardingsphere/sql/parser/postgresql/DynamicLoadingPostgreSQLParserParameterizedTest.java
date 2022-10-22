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

package org.apache.shardingsphere.sql.parser.postgresql;

import org.apache.shardingsphere.test.runner.ShardingSphereParallelTestParameterized;
import org.apache.shardingsphere.test.sql.parser.parameterized.engine.DynamicLoadingSQLParserParameterizedTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

@RunWith(ShardingSphereParallelTestParameterized.class)
public final class DynamicLoadingPostgreSQLParserParameterizedTest extends DynamicLoadingSQLParserParameterizedTest {
    
    public DynamicLoadingPostgreSQLParserParameterizedTest(final String sqlCaseId, final String sqlCaseValue) {
        super(sqlCaseId, sqlCaseValue, "PostgreSQL");
    }
    
    @Parameters(name = "{1} ({PostgreSQL}) -> {0}")
    public static Collection<Object[]> getTestParameters() throws IOException, URISyntaxException {
        return DynamicLoadingSQLParserParameterizedTest.getTestParameters("https://github.com/postgres/postgres/tree/master/src/test/regress/sql");
    }
}
