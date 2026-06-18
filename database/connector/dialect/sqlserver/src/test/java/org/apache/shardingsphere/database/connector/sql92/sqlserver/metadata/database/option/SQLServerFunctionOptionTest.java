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

package org.apache.shardingsphere.database.connector.sql92.sqlserver.metadata.database.option;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLServerFunctionOptionTest {
    
    private final SQLServerFunctionOption functionOption = new SQLServerFunctionOption();
    
    @Test
    void assertGetIfNullFunctionName() {
        assertThat(functionOption.getIfNullFunctionName(), is("IFNULL"));
    }
    
    @Test
    void assertGetUnparenthesizedFunctionNames() {
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("CURRENT_TIMESTAMP"));
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("CURRENT_USER"));
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("ROWNUM"));
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("ROWNUM_"));
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("ROW_NUMBER"));
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("SESSION_USER"));
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("SYSTEM_USER"));
        assertTrue(functionOption.getUnparenthesizedFunctionNames().contains("USER"));
    }
}
