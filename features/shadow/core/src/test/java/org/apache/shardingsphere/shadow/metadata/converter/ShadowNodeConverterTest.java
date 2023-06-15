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

package org.apache.shardingsphere.shadow.metadata.converter;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowNodeConverterTest {
    
    @Test
    void assertGetDataSourcePath() {
        assertThat(ShadowNodeConverter.getDataSourcePath("foo_db"), is("data_sources/foo_db"));
    }
    
    @Test
    void assertGetTableNamePath() {
        assertThat(ShadowNodeConverter.getTableNamePath("foo_table"), is("tables/foo_table"));
    }
    
    @Test
    void assertGetAlgorithmPath() {
        assertThat(ShadowNodeConverter.getShadowAlgorithmPath("SQL_HINT"), is("algorithms/SQL_HINT"));
    }
    
    @Test
    void assertGetDefaultShadowAlgorithmPath() {
        assertThat(ShadowNodeConverter.getDefaultShadowAlgorithmPath(), is("default_algorithm_name"));
    }
    
    @Test
    void assertCheckIsTargetRuleByRulePath() {
        assertTrue(ShadowNodeConverter.isShadowPath("/metadata/foo_db/rules/shadow/tables/foo_table"));
        assertFalse(ShadowNodeConverter.isShadowPath("/metadata/foo_db/rules/foo/tables/foo_table"));
        assertTrue(ShadowNodeConverter.isDataSourcePath("/metadata/foo_db/rules/shadow/data_sources/ds_shadow"));
        assertFalse(ShadowNodeConverter.isDataSourcePath("/metadata/foo_db/rules/shadow/tables/foo_table"));
        assertTrue(ShadowNodeConverter.isTablePath("/metadata/foo_db/rules/shadow/tables/foo_table"));
        assertFalse(ShadowNodeConverter.isTablePath("/metadata/foo_db/rules/shadow/algorithms/MD5"));
        assertTrue(ShadowNodeConverter.isAlgorithmPath("/metadata/foo_db/rules/shadow/algorithms/MD5"));
        assertFalse(ShadowNodeConverter.isAlgorithmPath("/metadata/foo_db/rules/shadow/tables/foo_table"));
        assertTrue(ShadowNodeConverter.isDefaultAlgorithmNamePath("/metadata/foo_db/rules/shadow/default_algorithm_name"));
        assertFalse(ShadowNodeConverter.isDefaultAlgorithmNamePath("/metadata/foo_db/rules/shadow/default_algorithm_name/s"));
    }
    
    @Test
    void assertGetDataSourceNameByRulePath() {
        Optional<String> actual = ShadowNodeConverter.getDataSourceName("/metadata/foo_db/rules/shadow/data_sources/foo_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertGetTableNameByRulePath() {
        Optional<String> actual = ShadowNodeConverter.getTableName("/metadata/foo_db/rules/shadow/tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetAlgorithmNameByRulePath() {
        Optional<String> actual = ShadowNodeConverter.getAlgorithmName("/metadata/foo_db/rules/shadow/algorithms/SQL_HINT");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("SQL_HINT"));
    }
}
