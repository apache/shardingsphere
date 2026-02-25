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

package org.apache.shardingsphere.single.checker.sql;

import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.checker.SupportedSQLCheckersBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.single.checker.sql.schema.SingleDropSchemaSupportedChecker;
import org.apache.shardingsphere.single.checker.sql.table.SingleDropTableSupportedChecker;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class SingleSupportedSQLCheckersBuilderTest {
    
    @SuppressWarnings("rawtypes")
    private final SupportedSQLCheckersBuilder builder = OrderedSPILoader.getServicesByClass(SupportedSQLCheckersBuilder.class, Collections.singleton(SingleRule.class)).get(SingleRule.class);
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetSupportedSQLCheckers() {
        List<SupportedSQLChecker<?, SingleRule>> actual = new ArrayList<SupportedSQLChecker<?, SingleRule>>(builder.getSupportedSQLCheckers());
        assertThat(actual.get(0), isA(SingleDropSchemaSupportedChecker.class));
        assertThat(actual.get(1), isA(SingleDropTableSupportedChecker.class));
    }
}
