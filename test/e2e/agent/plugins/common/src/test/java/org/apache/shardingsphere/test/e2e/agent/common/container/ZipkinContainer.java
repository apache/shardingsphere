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

package org.apache.shardingsphere.test.e2e.agent.common.container;

import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public final class ZipkinContainer extends DockerITContainer {
    
    private static final int EXPOSED_PORT = 9411;
    
    public ZipkinContainer(final String image) {
        super("zipkin", image);
    }
    
    @Override
    protected void configure() {
        withExposedPorts(EXPOSED_PORT);
        setWaitStrategy(new HttpWaitStrategy().forPort(EXPOSED_PORT));
    }
    
    @Override
    public String getAbbreviation() {
        return "zipkin";
    }
    
    public String getHttpUrl() {
        return String.format("http://%s:%s", getHost(), getMappedPort(EXPOSED_PORT));
    }
}
