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

package org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.jaxb.SQLNodeConverterTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;

import java.util.Map;

/**
 * SQL node converter test cases.
 */
@RequiredArgsConstructor
public final class SQLNodeConverterTestCases {
    
    private final Map<String, SQLNodeConverterTestCase> cases;
    
    /**
     * Get SQL node converter test case.
     *
     * @param sqlCaseId SQL case ID
     * @param sqlCaseType sql case type
     * @param databaseType database type
     * @return SQL node converter test case
     */
    public SQLNodeConverterTestCase get(final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        String caseKey = sqlCaseId + "_" + sqlCaseType + "_" + databaseType;
        return cases.get(caseKey);
    }
}
