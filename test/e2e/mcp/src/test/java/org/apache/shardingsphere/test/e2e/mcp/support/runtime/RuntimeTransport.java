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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.test.fixture.jdbc.H2RuntimeTestSupport.H2AccessMode;

/**
 * Runtime transport type for MCP E2E tests.
 */
@RequiredArgsConstructor
@Getter
public enum RuntimeTransport {
    
    HTTP(H2AccessMode.SINGLE_PROCESS),
    
    STDIO(H2AccessMode.MULTI_PROCESS);
    
    private final H2AccessMode h2AccessMode;
}
