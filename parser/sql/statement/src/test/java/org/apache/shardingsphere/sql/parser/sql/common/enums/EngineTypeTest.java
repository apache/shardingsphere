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

package org.apache.shardingsphere.sql.parser.sql.common.enums;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class EngineTypeTest {
    
    @Test
    void assertEngineType() {
        assertThat(EngineType.getEngineType("INNODB"), is(EngineType.INNODB));
        assertThat(EngineType.getEngineType("FEDERATED"), is(EngineType.FEDERATED));
        assertThat(EngineType.getEngineType("MEMORY"), is(EngineType.MEMORY));
        assertThat(EngineType.getEngineType("PERFORMANCE_SCHEMA"), is(EngineType.PERFORMANCE_SCHEMA));
        assertThat(EngineType.getEngineType("MYISAM"), is(EngineType.MYISAM));
        assertThat(EngineType.getEngineType("MRG_MYISAM"), is(EngineType.MRG_MYISAM));
        assertThat(EngineType.getEngineType("BLACKHOLE"), is(EngineType.BLACKHOLE));
        assertThat(EngineType.getEngineType("CsV"), is(EngineType.CSV));
        assertThat(EngineType.getEngineType("ARCHIVE"), is(EngineType.ARCHIVE));
        assertNull(EngineType.getEngineType("aaa"));
        assertNull(EngineType.getEngineType(""));
    }
}
