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

package org.apache.shardingsphere.sqlfederation.distsql.statement.updatable;

import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlterSQLFederationRuleStatementTest {
    
    @Test
    void assertLegacyConstructor() {
        CacheOptionSegment cacheOption = new CacheOptionSegment(4, 64L);
        AlterSQLFederationRuleStatement actual = new AlterSQLFederationRuleStatement(true, false, cacheOption);
        assertTrue(actual.getSqlFederationEnabled());
        assertFalse(actual.getAllQueryUseSQLFederation());
        assertThat(actual.getExecutionPlanCache(), sameInstance(cacheOption));
        assertNull(actual.getProviderType());
    }
}
