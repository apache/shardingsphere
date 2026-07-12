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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

class SQLServerFunctionOptionTest {
    
    private final SQLServerFunctionOption functionOption = new SQLServerFunctionOption();
    
    @Test
    void assertGetUnparenthesizedFunctionNames() {
        assertThat(functionOption.getUnparenthesizedFunctionNames(), hasItems("CURRENT_TIMESTAMP", "CURRENT_USER", "SESSION_USER", "SYSTEM_USER", "USER"));
        assertThat(functionOption.getUnparenthesizedFunctionNames(), not(hasItem("ROWNUM")));
        assertThat(functionOption.getUnparenthesizedFunctionNames(), not(hasItem("ROWNUM_")));
        assertThat(functionOption.getUnparenthesizedFunctionNames(), not(hasItem("ROW_NUMBER")));
    }
}
