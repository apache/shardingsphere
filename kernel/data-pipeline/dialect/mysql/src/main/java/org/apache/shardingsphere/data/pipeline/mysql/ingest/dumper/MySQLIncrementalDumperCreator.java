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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.dumper;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.MySQLIncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.DialectIncrementalDumperCreator;

/**
 * MySQL incremental dumper creator.
 */
public final class MySQLIncrementalDumperCreator implements DialectIncrementalDumperCreator {
    
    @Override
    public IncrementalDumper createIncrementalDumper(final IncrementalDumperContext context, final IngestPosition position,
                                                     final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        return new MySQLIncrementalDumper(context, position, channel, metaDataLoader);
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
