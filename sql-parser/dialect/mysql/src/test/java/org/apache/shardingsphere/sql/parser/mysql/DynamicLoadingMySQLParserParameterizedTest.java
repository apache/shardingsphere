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

package org.apache.shardingsphere.sql.parser.mysql;

import org.apache.shardingsphere.test.sql.parser.parameterized.engine.DynamicLoadingSQLParserParameterizedTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

//@RunWith(ShardingSphereParallelTestParameterized.class)
public final class DynamicLoadingMySQLParserParameterizedTest extends DynamicLoadingSQLParserParameterizedTest {
    
    public DynamicLoadingMySQLParserParameterizedTest(final String sqlCaseId, final String sqlCaseValue) {
        super(sqlCaseId, sqlCaseValue, "MySQL");
    }
    
    @Parameters(name = "{0} (MySQL) -> {1}")
    public static Collection<Object[]> getTestParameters() throws IOException {
        return Collections.emptyList();
        // return DynamicLoadingSQLParserParameterizedTest.getTestParameters("https://github.com/mysql/mysql-server/tree/8.0/mysql-test/t");
    }
}
