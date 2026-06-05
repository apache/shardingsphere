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

package org.apache.shardingsphere.mcp.support.workflow.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ValidationReportTest {
    
    @Test
    void assertToMap() {
        ValidationReport validationReport = new ValidationReport();
        validationReport.setRuleValidation(new ValidationSection("passed", Map.of(), "ok"));
        validationReport.setOverallStatus("passed");
        Map<String, Object> actual = validationReport.toMap();
        assertFalse(actual.containsKey("ddl_validation"));
        assertFalse(actual.containsKey("logical_metadata_validation"));
        assertFalse(actual.containsKey("sql_executability_validation"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
        assertThat(actual.get("overall_status"), is("passed"));
    }
}
