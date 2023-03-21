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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import io.netty.channel.Channel;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.core.connector.SocketSinkImporterConnector;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public final class SocketSinkImporterCreatorTest {
    
    @Mock
    private ImporterConfiguration importerConfig;
    
    @Test
    public void assertCreateCDCImporter() {
        SocketSinkImporterConnector importerConnector = new SocketSinkImporterConnector(mock(Channel.class), mock(ShardingSphereDatabase.class), 1, Collections.emptyList(), null);
        assertThat(TypedSPILoader.getService(ImporterCreator.class, "Socket").createImporter(importerConfig, importerConnector, null, null, null), instanceOf(SocketSinkImporter.class));
    }
}
