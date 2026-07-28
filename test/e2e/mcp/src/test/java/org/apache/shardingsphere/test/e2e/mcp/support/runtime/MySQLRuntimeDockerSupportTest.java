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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLRuntimeDockerSupportTest {
    
    @Test
    void assertCreateDockerRequiredMessageWithReadinessDiagnostic() {
        assertThat(MySQLRuntimeDockerSupport.createDockerRequiredMessage("Docker is required.", "daemon unavailable"),
                is("Docker is required. Docker readiness diagnostic: daemon unavailable"));
    }
    
    @Test
    void assertCreateDockerRequiredMessageWithoutReadinessDiagnostic() {
        assertThat(MySQLRuntimeDockerSupport.createDockerRequiredMessage("Docker is required.", ""), is("Docker is required."));
    }
    
    @Test
    void assertGetMySQLImage() {
        Properties props = new Properties();
        props.setProperty("mcp.e2e.mysql.image", "mysql:8.4.0");
        assertThat(MySQLRuntimeDockerSupport.getMySQLImage(props), is("mysql:8.4.0"));
    }
    
    @Test
    void assertGetMySQLImageWithMissingProperty() {
        IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> MySQLRuntimeDockerSupport.getMySQLImage(new Properties()));
        assertThat(actualException.getMessage(), is("MCP E2E MySQL image property `mcp.e2e.mysql.image` is required."));
    }
}
