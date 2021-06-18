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

package org.apache.shardingsphere.scaling.distsql.parser.core;

import org.apache.shardingsphere.distsql.parser.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingJobListStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

// TODO use Parameterized + XML instead of static test
public final class ScalingStatementParserEngineTest {
    
    private static final String SHOW_SCALING_JOB_LIST = "SHOW SCALING JOB LIST;";
    
    private final DistSQLStatementParserEngine engine = new DistSQLStatementParserEngine();
    
    @Test
    public void assertParseShowScalingJobList() {
        SQLStatement sqlStatement = engine.parse(SHOW_SCALING_JOB_LIST);
        assertTrue(sqlStatement instanceof ShowScalingJobListStatement);
    }
    
    // TODO add more test cases
}
