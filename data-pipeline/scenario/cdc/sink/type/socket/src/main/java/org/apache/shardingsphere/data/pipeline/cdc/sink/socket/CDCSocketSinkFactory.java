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

package org.apache.shardingsphere.data.pipeline.cdc.sink.socket;

import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSink;
import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSinkCreationContext;
import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSinkFactory;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

/**
 * CDC socket sink factory.
 */
public final class CDCSocketSinkFactory implements CDCSinkFactory {
    
    @Override
    public CDCSink create(final CDCSinkCreationContext context) {
        ShardingSpherePreconditions.checkState(context instanceof CDCSocketSinkCreationContext, () -> new IllegalArgumentException("Socket sink requires a socket session creation context."));
        CDCSocketSinkCreationContext socketContext = (CDCSocketSinkCreationContext) context;
        ShardingSpherePreconditions.checkNotNull(socketContext.getChannel(), () -> new IllegalArgumentException("Socket sink requires a channel."));
        return new PipelineCDCSocketSink(socketContext.getChannel(), socketContext.getDatabase(), socketContext.getSchemaTableNames());
    }
    
    @Override
    public String getType() {
        return "SOCKET";
    }
}
