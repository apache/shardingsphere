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

package org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DateTimeLiteralValueTest {
    
    @Test
    void assertGetValue() {
        assertThat(new DateTimeLiteralValue("DATE", "'2024-03-01'", false).getValue(), is("DATE '2024-03-01'"));
    }
    
    @Test
    void assertGetValueWithBrace() {
        assertThat(new DateTimeLiteralValue("ts", "'2024-03-01 10:00:00'", true).getValue(), is("{ts '2024-03-01 10:00:00'}"));
    }
    
    @Test
    void assertGetDateTimeValue() {
        assertThat(new DateTimeLiteralValue("ts", "'2024-03-01 10:00:00'", true).getDateTimeValue(), is("2024-03-01 10:00:00"));
    }
}
