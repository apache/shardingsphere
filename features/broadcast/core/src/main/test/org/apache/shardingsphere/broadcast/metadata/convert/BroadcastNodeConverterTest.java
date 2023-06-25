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

package org.apache.shardingsphere.broadcast.metadata.convert;

import org.apache.shardingsphere.broadcast.metadata.converter.BroadcastNodeConverter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcastNodeConverterTest {
    
    @Test
    void assertGetTableNamePath() {
        assertThat(BroadcastNodeConverter.getTablesPath(), is("tables"));
    }
    
    @Test
    void assertCheckIsTargetRuleByRulePath() {
        assertTrue(BroadcastNodeConverter.isBroadcastPath("/metadata/foo_db/rules/broadcast/tables"));
        assertFalse(BroadcastNodeConverter.isBroadcastPath("/metadata/foo_db/rules/foo/tables/foo_table"));
        assertTrue(BroadcastNodeConverter.isTablesActiveVersionPath("/metadata/foo_db/rules/broadcast/tables/active_version"));
        assertFalse(BroadcastNodeConverter.isTablesActiveVersionPath("/metadata/foo_db/rules/broadcast/tables/versions/0"));
    }
}
