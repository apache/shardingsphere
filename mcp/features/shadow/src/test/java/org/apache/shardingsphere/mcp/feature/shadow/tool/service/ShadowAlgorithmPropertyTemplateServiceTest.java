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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowAlgorithmPropertyTemplateServiceTest {
    
    private final ShadowAlgorithmPropertyTemplateService service = new ShadowAlgorithmPropertyTemplateService();
    
    @Test
    void assertFindRequirementsForValueMatch() {
        List<AlgorithmPropertyRequirement> actual = service.findRequirements("VALUE_MATCH");
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0).getPropertyKey(), is("operation"));
        assertThat(actual.get(1).getPropertyKey(), is("column"));
        assertThat(actual.get(2).getPropertyKey(), is("value"));
    }
    
    @Test
    void assertFindRequirementsForRegexMatch() {
        assertThat(service.findRequirements("REGEX_MATCH").get(2).getPropertyKey(), is("regex"));
    }
    
    @Test
    void assertFindRequirementsForSqlHint() {
        assertTrue(service.findRequirements("SQL_HINT").isEmpty());
    }
}
