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

package org.apache.shardingsphere.data.pipeline.cdc.core.task;

import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CDCIncrementalTaskTest {
    
    @Test
    void assertRunTask() throws ExecutionException, InterruptedException {
        Dumper mockDumper = mock(Dumper.class);
        Importer mockImporter = mock(Importer.class);
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress(new IngestPlaceholderPosition());
        try (CDCIncrementalTask cdcIncrementalTask = new CDCIncrementalTask("test", ExecuteEngine.newFixedThreadInstance(5, "test"), mockDumper, mockImporter, incrementalTaskProgress)) {
            Collection<CompletableFuture<?>> futures = cdcIncrementalTask.start();
            for (CompletableFuture<?> each : futures) {
                each.get();
            }
            verify(mockDumper).run();
            verify(mockImporter).run();
            cdcIncrementalTask.stop();
            verify(mockDumper).stop();
            verify(mockImporter).stop();
        }
    }
}
