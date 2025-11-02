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

package org.apache.shardingsphere.infra.version;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for ShardingSphereVersion.
 */
class ShardingSphereVersionTest {
    
    @Test
    void assertVersion() {
        assertTrue(ShardingSphereVersion.VERSION.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
        assertTrue(Integer.parseInt(ShardingSphereVersion.VERSION.split("-")[0].split("\\.")[0]) >= 1);
    }
    
    @Test
    void assertIsSnapshot() {
        assertThat(ShardingSphereVersion.IS_SNAPSHOT, is(ShardingSphereVersion.VERSION.endsWith("-SNAPSHOT")));
    }
    
    @Test
    void assertBuildBranch() {
        assertNotNull(ShardingSphereVersion.BUILD_BRANCH);
    }
    
    @Test
    void assertBuildTime() {
        assertNotNull(ShardingSphereVersion.BUILD_TIME);
    }
    
    @Test
    void assertCommitId() {
        assertNotNull(ShardingSphereVersion.BUILD_COMMIT_ID);
    }
    
    @Test
    void assertCommitIdAbbrev() {
        assertNotNull(ShardingSphereVersion.BUILD_COMMIT_ID_ABBREV);
    }
    
    @Test
    void assertCommitMessageShort() {
        assertNotNull(ShardingSphereVersion.BUILD_COMMIT_MESSAGE_SHORT);
    }
    
    @Test
    void assertTag() {
        assertNotNull(ShardingSphereVersion.BUILD_TAG);
    }
}
