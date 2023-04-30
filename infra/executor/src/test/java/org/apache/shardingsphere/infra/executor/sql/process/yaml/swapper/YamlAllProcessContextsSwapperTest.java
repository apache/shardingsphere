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

package org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper;

import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessListContexts;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlAllProcessContextsSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        ExecutionGroupReportContext reportContext = new ExecutionGroupReportContext("foo_db", new Grantee("root", "localhost"));
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(Collections.emptyList(), reportContext);
        ProcessContext processContext = new ProcessContext("SELECT 1", executionGroupContext);
        YamlProcessListContexts actual = new YamlProcessListContextsSwapper().swapToYamlConfiguration(Collections.singleton(processContext));
        assertThat(actual.getContexts().size(), is(1));
        assertYamlProcessContext(actual.getContexts().iterator().next());
    }
    
    private static void assertYamlProcessContext(final YamlProcessContext actual) {
        assertNotNull(actual.getProcessID());
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getHostname(), is("localhost"));
        assertThat(actual.getSql(), is("SELECT 1"));
        assertThat(actual.getCompletedUnitCount(), is(0));
        assertThat(actual.getTotalUnitCount(), is(0));
        assertThat(actual.getStartTimeMillis(), lessThanOrEqualTo(System.currentTimeMillis()));
        assertFalse(actual.isIdle());
    }
    
    @Test
    void assertSwapToObject() {
        assertThrows(UnsupportedOperationException.class, () -> new YamlProcessListContextsSwapper().swapToObject(new YamlProcessListContexts()));
    }
}
