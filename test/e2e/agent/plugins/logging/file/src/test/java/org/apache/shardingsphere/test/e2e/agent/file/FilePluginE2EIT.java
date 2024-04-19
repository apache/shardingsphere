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

package org.apache.shardingsphere.test.e2e.agent.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.agent.common.AgentTestActionExtension;
import org.apache.shardingsphere.test.e2e.agent.common.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.file.asserts.ContentAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(AgentTestActionExtension.class)
@Slf4j
class FilePluginE2EIT {
    
    @Test
    void assertWithAgent() {
        Collection<String> actualLogLines = E2ETestEnvironment.getInstance().getActualLogs();
        log.info("actualLogLines size:{}", actualLogLines.size());
        assertFalse(actualLogLines.isEmpty(), "Actual log is empty");
        if (AdapterType.PROXY.getValue().equalsIgnoreCase(E2ETestEnvironment.getInstance().getAdapter())) {
            assertProxyWithAgent(actualLogLines);
        } else {
            assertJdbcWithAgent(actualLogLines);
        }
    }
    
    private void assertProxyWithAgent(final Collection<String> actualLogLines) {
        ContentAssert.assertIs(actualLogLines, "Build meta data contexts finished, cost\\s(?=[1-9]+\\d*)");
    }
    
    private void assertJdbcWithAgent(final Collection<String> actualLogLines) {
        ContentAssert.assertIs(actualLogLines, "Build meta data contexts finished, cost\\s(?=[1-9]+\\d*)");
    }
}
