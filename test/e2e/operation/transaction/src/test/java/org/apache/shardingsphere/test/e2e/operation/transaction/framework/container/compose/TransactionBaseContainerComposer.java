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

package org.apache.shardingsphere.test.e2e.operation.transaction.framework.container.compose;

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.env.container.E2EContainers;
import org.testcontainers.lifecycle.Startable;

/**
 * Transaction base container composer.
 */
@Getter
public abstract class TransactionBaseContainerComposer implements Startable {
    
    private final E2EContainers containers;
    
    protected TransactionBaseContainerComposer(final String scenario) {
        containers = new E2EContainers(scenario);
    }
    
    @Override
    public void start() {
        containers.start();
    }
    
    @Override
    public void stop() {
        containers.stop();
    }
}
