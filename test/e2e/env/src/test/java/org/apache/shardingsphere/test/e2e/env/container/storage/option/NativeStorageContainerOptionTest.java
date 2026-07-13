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

package org.apache.shardingsphere.test.e2e.env.container.storage.option;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NativeStorageContainerOptionTest {
    
    private final NativeStorageContainerOption option = new FixtureNativeStorageContainerOption();
    
    @Test
    void assertGetInitURL() {
        StorageContainerConnectOption connectOption = mock(StorageContainerConnectOption.class);
        when(connectOption.getURL("127.0.0.1", 3306)).thenReturn("jdbc:fixture://127.0.0.1:3306");
        assertThat(option.getInitURL(connectOption, "127.0.0.1", 3306), is("jdbc:fixture://127.0.0.1:3306"));
    }
    
    @Test
    void assertGetAccessURL() {
        StorageContainerConnectOption connectOption = mock(StorageContainerConnectOption.class);
        when(connectOption.getURL("127.0.0.1", 3306, "foo_ds")).thenReturn("jdbc:fixture://127.0.0.1:3306/foo_ds");
        assertThat(option.getAccessURL(connectOption, "127.0.0.1", 3306, "foo_ds"), is("jdbc:fixture://127.0.0.1:3306/foo_ds"));
    }
    
    @Test
    void assertGetAccessURLWithoutDataSourceName() {
        StorageContainerConnectOption connectOption = mock(StorageContainerConnectOption.class);
        when(connectOption.getURL("127.0.0.1", 3306)).thenReturn("jdbc:fixture://127.0.0.1:3306");
        assertThat(option.getAccessURL(connectOption, "127.0.0.1", 3306, ""), is("jdbc:fixture://127.0.0.1:3306"));
    }
    
    @Test
    void assertGetLinkReplacements() {
        assertThat(option.getLinkReplacements(mock(StorageContainerConnectOption.class), "fixture.host", "127.0.0.1", 3307, 3306),
                is(Collections.singletonMap("fixture.host:3306", "127.0.0.1:3307")));
    }
    
    private static final class FixtureNativeStorageContainerOption implements NativeStorageContainerOption {
        
        @Override
        public int getMajorVersion() {
            return 0;
        }
        
        @Override
        public String getDatabaseType() {
            return "Fixture";
        }
    }
}
