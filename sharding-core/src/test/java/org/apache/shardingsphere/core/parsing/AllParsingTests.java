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

package org.apache.shardingsphere.core.parsing;

import org.apache.shardingsphere.core.parsing.integrate.AllParsingIntegrateTests;
import org.apache.shardingsphere.core.parsing.lexer.AllLexerTests;
import org.apache.shardingsphere.core.parsing.parser.constant.AllConstantTests;
import org.apache.shardingsphere.core.parsing.parser.context.AllContextTests;
import org.apache.shardingsphere.core.parsing.parser.sql.AllSQLTests;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesTokenTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllLexerTests.class, 
        AllSQLTests.class, 
        SQLJudgeEngineTest.class,
        AllContextTests.class,
        AllConstantTests.class,
        InsertValuesTokenTest.class,
        AllParsingIntegrateTests.class
})
public final class AllParsingTests {
}
