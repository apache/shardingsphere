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

package org.apache.shardingsphere.mode.metadata.manager;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.DatabaseMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveVersionCheckerTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertCheckSameWithMatchedDataChangedEvent() {
        when(repository.query("foo_active_version_path")).thenReturn("1");
        assertTrue(new ActiveVersionChecker(repository).checkSame(new DataChangedEvent("foo_active_version_path", "1", DataChangedEvent.Type.UPDATED)));
    }
    
    @Test
    void assertCheckSameWithVersionNodePathAndMismatchedVersion() {
        VersionNodePath versionNodePath = new VersionNodePath(new DatabaseMetaDataNodePath("foo_db"));
        when(repository.query(versionNodePath.getActiveVersionPath())).thenReturn("2");
        assertFalse(new ActiveVersionChecker(repository).checkSame(versionNodePath, 5));
    }
}
