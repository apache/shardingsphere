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

package org.apache.shardingsphere.data.pipeline.cdc.sink.spi;

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * Stateless factory for CDC sinks.
 * Implementations must keep connection and delivery state in each created sink rather than in the shared factory.
 */
@SingletonSPI
public interface CDCSinkFactory extends TypedSPI {
    
    /**
     * Create a sink bound to the supplied runtime context.
     *
     * @param context metadata, sink properties and implementation-specific runtime context
     * @return new sink for the job or session
     */
    CDCSink create(CDCSinkCreationContext context);
}
