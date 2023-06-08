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

package org.apache.shardingsphere.readwritesplitting.metadata.converter;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingNodeConverterTest {
    
    @Test
    void assertGetGroupNamePath() {
        assertThat(ReadwriteSplittingNodeConverter.getGroupNamePath("group_0"), is("data_sources/group_0"));
    }
    
    @Test
    void assertGetLoadBalancerPath() {
        assertThat(ReadwriteSplittingNodeConverter.getLoadBalancerPath("random"), is("load_balancers/random"));
    }
    
    @Test
    void assertCheckIsTargetRuleByRulePath() {
        assertTrue(ReadwriteSplittingNodeConverter.isReadwriteSplittingPath("/metadata/foo_db/rules/readwrite_splitting/data_sources/group_0"));
        assertFalse(ReadwriteSplittingNodeConverter.isReadwriteSplittingPath("/metadata/foo_db/rules/foo/data_sources/group_0"));
        assertTrue(ReadwriteSplittingNodeConverter.isDataSourcePath("/metadata/foo_db/rules/readwrite_splitting/data_sources/group_0"));
        assertFalse(ReadwriteSplittingNodeConverter.isDataSourcePath("/metadata/foo_db/rules/readwrite_splitting/load_balancers/random"));
        assertTrue(ReadwriteSplittingNodeConverter.isLoadBalancerPath("/metadata/foo_db/rules/readwrite_splitting/load_balancers/random"));
        assertFalse(ReadwriteSplittingNodeConverter.isLoadBalancerPath("/metadata/foo_db/rules/readwrite_splitting/data_sources/group_0"));
    }
    
    @Test
    void assertGetGroupNameByRulePath() {
        Optional<String> actual = ReadwriteSplittingNodeConverter.getGroupName("/metadata/foo_db/rules/readwrite_splitting/data_sources/group_0/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("group_0"));
    }
    
    @Test
    void assertGetLoadBalancerNameByRulePath() {
        Optional<String> actual = ReadwriteSplittingNodeConverter.getLoadBalancerName("/metadata/foo_db/rules/readwrite_splitting/load_balancers/random");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("random"));
    }
}
