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
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcess;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessList;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlProcessListSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        String processId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
        ExecutionGroupReportContext reportContext = new ExecutionGroupReportContext(processId, "foo_db", new Grantee("root", "localhost"));
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(Collections.emptyList(), reportContext);
        Process process = new Process("SELECT 1", executionGroupContext);
        YamlProcessList actual = new YamlProcessListSwapper().swapToYamlConfiguration(Collections.singleton(process));
        assertThat(actual.getProcesses().size(), is(1));
        assertYamlProcessContext(actual.getProcesses().iterator().next());
    }
    
    private void assertYamlProcessContext(final YamlProcess actual) {
        assertNotNull(actual.getId());
        assertThat(actual.getStartMillis(), lessThanOrEqualTo(System.currentTimeMillis()));
        assertThat(actual.getSql(), is("SELECT 1"));
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getHostname(), is("localhost"));
        assertThat(actual.getCompletedUnitCount(), is(0));
        assertThat(actual.getTotalUnitCount(), is(0));
        assertFalse(actual.isIdle());
    }
    
    @Test
    void assertSwapToObject() {
        YamlProcessList yamlProcessList = new YamlProcessList();
        yamlProcessList.setProcesses(Collections.singleton(createYamlProcess()));
        Collection<Process> actual = new YamlProcessListSwapper().swapToObject(yamlProcessList);
        assertThat(actual.size(), is(1));
        assertProcess(actual.iterator().next());
    }
    
    private YamlProcess createYamlProcess() {
        YamlProcess result = new YamlProcess();
        result.setId("foo_id");
        result.setStartMillis(1000L);
        result.setSql("SELECT 1");
        result.setDatabaseName("foo_db");
        result.setUsername("root");
        result.setHostname("localhost");
        result.setTotalUnitCount(10);
        result.setCompletedUnitCount(5);
        result.setIdle(true);
        return result;
    }
    
    private void assertProcess(final Process actual) {
        assertThat(actual.getId(), is("foo_id"));
        assertThat(actual.getStartMillis(), is(1000L));
        assertThat(actual.getSql(), is("SELECT 1"));
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getHostname(), is("localhost"));
        assertThat(actual.getTotalUnitCount(), is(10));
        assertThat(actual.getCompletedUnitCount(), is(5));
        assertTrue(actual.isIdle());
    }
}
