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

package org.apache.shardingsphere.infra.exception.external.server;

import org.apache.shardingsphere.infra.exception.external.server.fixture.ShardingSphereFixtureServerException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingSphereServerExceptionTest {
    
    @Test
    void assertGetMessage() {
        assertThat(new ShardingSphereFixtureServerException().getMessage(), is("FIXTURE-00001: Fixture error message"));
    }
    
    @Test
    void assertGetCause() {
        RuntimeException cause = new RuntimeException("Test");
        assertThat(new ShardingSphereFixtureServerException(cause).getCause(), is(cause));
    }
}
