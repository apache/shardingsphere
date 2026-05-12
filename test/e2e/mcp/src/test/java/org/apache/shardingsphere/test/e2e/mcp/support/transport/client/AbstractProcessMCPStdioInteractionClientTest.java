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

package org.apache.shardingsphere.test.e2e.mcp.support.transport.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledOnOs({OS.LINUX, OS.MAC})
class AbstractProcessMCPStdioInteractionClientTest {
    
    @Test
    void assertRuntimeFailureKeepsProcessFailureAndStderrDiagnostics() {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new FailingStdioInteractionClient().open());
        assertThat(actual.getMessage(), containsString("STDIO MCP runtime did not return a response."));
        assertThat(actual.getMessage(), containsString("Process failure: child boom."));
        assertThat(actual.getMessage(), containsString("stderr: Exception in thread"));
        assertThat(actual.getMessage(), containsString("stderr detail"));
    }
    
    private static final class FailingStdioInteractionClient extends AbstractProcessMCPStdioInteractionClient {
        
        @Override
        protected ProcessBuilder createProcessBuilder() throws IOException {
            return new ProcessBuilder("sh", "-c",
                    "printf 'Exception in thread \"main\" java.lang.IllegalStateException: child boom\\nstderr detail\\n' >&2; sleep 1; exit 1");
        }
        
        @Override
        protected String getClientName() {
            return "failing-stdio";
        }
    }
}
