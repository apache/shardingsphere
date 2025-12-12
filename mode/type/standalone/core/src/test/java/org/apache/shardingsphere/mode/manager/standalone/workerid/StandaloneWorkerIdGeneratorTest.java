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

package org.apache.shardingsphere.mode.manager.standalone.workerid;

import org.apache.shardingsphere.infra.instance.workerid.WorkerIdAssignedException;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandaloneWorkerIdGeneratorTest {
    
    @Test
    void assertGenerateWithEmptyProperties() {
        assertThat(new StandaloneWorkerIdGenerator().generate(new Properties()), is(0));
    }
    
    @Test
    void assertGenerateWithProperties() {
        assertThat(new StandaloneWorkerIdGenerator().generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "1"))), is(1));
    }
    
    @Test
    void assertGenerateWithInvalidProperties() {
        assertThrows(WorkerIdAssignedException.class, () -> new StandaloneWorkerIdGenerator().generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "-1"))));
        assertThrows(WorkerIdAssignedException.class, () -> new StandaloneWorkerIdGenerator().generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "1024"))));
    }
}
