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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;

import java.util.Map;

/**
 * SQL parser test cases.
 */
@RequiredArgsConstructor
public final class SQLParserTestCases {
    
    private final Map<String, SQLParserTestCase> cases;
    
    /**
     * Get SQL parser test case.
     *
     * @param caseId SQL case ID
     * @return got case
     */
    public SQLParserTestCase get(final String caseId) {
        Preconditions.checkState(cases.containsKey(caseId), "Can not find SQL of ID: %s.", caseId);
        return cases.get(caseId);
    }
}
