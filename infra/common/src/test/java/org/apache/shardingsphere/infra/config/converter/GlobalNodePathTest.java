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

package org.apache.shardingsphere.infra.config.converter;

import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalNodePathTest {
    
    @Test
    void assertGetVersion() {
        Optional<String> actual = GlobalNodePath.getVersion("transaction", "/rules/transaction/versions/0");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("0"));
    }
    
    @Test
    void assertIsRuleActiveVersionPath() {
        assertTrue(GlobalNodePath.isRuleActiveVersionPath("/rules/transaction/active_version"));
    }
    
    @Test
    void assertIsPropsActiveVersionPath() {
        assertTrue(GlobalNodePath.isPropsActiveVersionPath("/props/active_version"));
    }
    
    @Test
    void assertGetRuleName() {
        Optional<String> actual = GlobalNodePath.getRuleName("/rules/transaction/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("transaction"));
    }
}
