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

package org.apache.shardingsphere.encrypt.checker.sql;

import org.apache.shardingsphere.encrypt.checker.sql.orderby.EncryptOrderByItemSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.predicate.EncryptPredicateColumnSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.projection.EncryptInsertSelectProjectionSupportedChecker;
import org.apache.shardingsphere.encrypt.checker.sql.projection.EncryptSelectProjectionSupportedChecker;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.checker.SupportedSQLCheckersBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class EncryptSupportedSQLCheckersBuilderTest {
    
    @SuppressWarnings("rawtypes")
    private SupportedSQLCheckersBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = OrderedSPILoader.getServicesByClass(SupportedSQLCheckersBuilder.class, Collections.singleton(EncryptRule.class)).get(EncryptRule.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetSupportedSQLCheckers() {
        List<SupportedSQLChecker<?, EncryptRule>> actual = new ArrayList<SupportedSQLChecker<?, EncryptRule>>(builder.getSupportedSQLCheckers());
        assertThat(actual.get(0), isA(EncryptSelectProjectionSupportedChecker.class));
        assertThat(actual.get(1), isA(EncryptInsertSelectProjectionSupportedChecker.class));
        assertThat(actual.get(2), isA(EncryptPredicateColumnSupportedChecker.class));
        assertThat(actual.get(3), isA(EncryptOrderByItemSupportedChecker.class));
    }
}
