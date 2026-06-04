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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;

abstract class AbstractTransportParameterizedProductionRuntimeE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private RuntimeTransport transport;
    
    protected final void useTransport(final RuntimeTransport transport) {
        this.transport = transport;
    }
    
    @Override
    protected final RuntimeTransport getTransport() {
        if (null == transport) {
            throw new IllegalStateException("Runtime transport is not selected for current production E2E test.");
        }
        return transport;
    }
}
