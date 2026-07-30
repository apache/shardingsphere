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

package org.apache.shardingsphere.test.e2e.mcp.env;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPE2ETestConfigurationTest {
    
    @Test
    void assertDefaultRunType() {
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(new Properties());
        assertFalse(config.isDockerRunType());
    }
    
    @Test
    void assertDockerRunType() {
        Properties props = new Properties();
        props.setProperty("e2e.run.type", "DOCKER");
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(props);
        assertTrue(config.isDockerRunType());
    }
    
    @Test
    void assertCommaSeparatedRunTypes() {
        Properties props = new Properties();
        props.setProperty("e2e.run.type", "NATIVE, docker");
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(props);
        assertTrue(config.isDockerRunType());
    }
    
    @Test
    void assertNativeRunType() {
        Properties props = new Properties();
        props.setProperty("e2e.run.type", "NATIVE");
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(props);
        assertFalse(config.isDockerRunType());
    }
    
    @Test
    void assertInvalidRunType() {
        Properties props = new Properties();
        props.setProperty("e2e.run.type", "DOCKER, REMOTE");
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(props);
        IllegalStateException actualException = assertThrows(IllegalStateException.class, config::isDockerRunType);
        assertThat(actualException.getMessage(), is("Unsupported MCP E2E run type `REMOTE`."));
    }
    
}
