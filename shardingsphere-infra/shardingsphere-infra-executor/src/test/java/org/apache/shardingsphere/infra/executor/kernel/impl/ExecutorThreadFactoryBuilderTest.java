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

package org.apache.shardingsphere.infra.executor.kernel.impl;

import org.junit.Test;

import java.util.concurrent.ThreadFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ExecutorThreadFactoryBuilderTest {
    
    @Test
    public void assertBuild() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build();
        Thread thread0 = threadFactory.newThread(() -> { });
        assertThat(thread0.getName(), is("ShardingSphere-0"));
        Thread thread1 = threadFactory.newThread(() -> { });
        assertThat(thread1.getName(), is("ShardingSphere-1"));
    }
    
    @Test
    public void assertBuildWithNameFormat() {
        ThreadFactory threadFactory = ExecutorThreadFactoryBuilder.build("test");
        Thread thread = threadFactory.newThread(() -> { });
        assertThat(thread.getName(), is("ShardingSphere-test"));
    }
}
