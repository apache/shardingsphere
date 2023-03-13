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

import org.apache.shardingsphere.test.e2e.agent.common.AgentTestActionExtension;
import org.apache.shardingsphere.test.e2e.agent.file.asserts.ContentAssert;
import org.apache.shardingsphere.test.e2e.agent.file.loader.LogLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AgentTestActionExtension.class)
public final class FilePluginE2EIT {
    
    @Test
    public void assertProxyWithAgent() {
        assertTrue(LogLoader.getLogFile().exists(), String.format("The file `%s` does not exist", LogLoader.getLogFilePath()));
        ContentAssert.assertIs("Build meta data contexts finished, cost\\s(?=[1-9]+\\d*)");
    }
}
