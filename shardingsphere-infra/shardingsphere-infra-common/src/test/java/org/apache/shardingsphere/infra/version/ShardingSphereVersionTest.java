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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereVersionTest {
    
    @Test
    public void assertVersionInfo() {
        assertThat(ShardingSphereVersion.BUILD_BRANCH, is("test_branch"));
        assertThat(ShardingSphereVersion.BUILD_TIME, is("2021-01-01T00:00:00+0000"));
        assertThat(ShardingSphereVersion.BUILD_MAVEN_PROJECT_VERSION, is("test_version"));
        assertThat(ShardingSphereVersion.BUILD_GIT_COMMIT_ID, is("test_commit_id"));
        assertThat(ShardingSphereVersion.BUILD_GIT_COMMIT_ID_ABBREV, is("test_commit_id_abbrev"));
        assertThat(ShardingSphereVersion.BUILD_GIT_COMMIT_MESSAGE_SHORT, is("test_commit_message"));
        assertThat(ShardingSphereVersion.BUILD_GIT_TAG, is("test_tag"));
        assertTrue(ShardingSphereVersion.BUILD_GIT_DIRTY);
    }
}
