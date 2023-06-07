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

package org.apache.shardingsphere.mask.metadata.converter;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskNodeConverterTest {
    
    @Test
    void assertGetTableNamePath() {
        assertThat(MaskNodeConverter.getTableNamePath("foo_table"), is("tables/foo_table"));
    }
    
    @Test
    void assertGetAlgorithmPath() {
        assertThat(MaskNodeConverter.getMaskAlgorithmPath("MD5"), is("algorithms/MD5"));
    }
    
    @Test
    void assertCheckIsTargetRuleByRulePath() {
        assertTrue(MaskNodeConverter.isMaskPath("/metadata/foo_db/rules/mask/tables/foo_table"));
        assertFalse(MaskNodeConverter.isMaskPath("/metadata/foo_db/rules/foo/tables/foo_table"));
        assertTrue(MaskNodeConverter.isTablePath("/metadata/foo_db/rules/mask/tables/foo_table"));
        assertFalse(MaskNodeConverter.isTablePath("/metadata/foo_db/rules/mask/algorithms/MD5"));
        assertTrue(MaskNodeConverter.isAlgorithmPath("/metadata/foo_db/rules/mask/algorithms/MD5"));
        assertFalse(MaskNodeConverter.isAlgorithmPath("/metadata/foo_db/rules/mask/tables/foo_table"));
    }
    
    @Test
    void assertGetTableNameByRulePath() {
        Optional<String> actual = MaskNodeConverter.getTableName("/metadata/foo_db/rules/mask/tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetAlgorithmNameByRulePath() {
        Optional<String> actual = MaskNodeConverter.getAlgorithmName("/metadata/foo_db/rules/mask/algorithms/MD5");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("MD5"));
    }
}
