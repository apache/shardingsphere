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

package org.apache.shardingsphere.data.pipeline.common.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;

import java.util.Objects;

/**
 * Pipeline context key.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class PipelineContextKey {
    
    private final String databaseName;
    
    private final InstanceType instanceType;
    
    /**
     * Build context key.
     *
     * @param databaseName database name
     * @param instanceType instance type
     * @return context key
     */
    public static PipelineContextKey build(final String databaseName, final InstanceType instanceType) {
        return new PipelineContextKey(databaseName, instanceType);
    }
    
    /**
     * Build context key for proxy.
     *
     * @return context key
     */
    public static PipelineContextKey buildForProxy() {
        return new PipelineContextKey("", InstanceType.PROXY);
    }
    
    /**
     * Build context key for proxy.
     *
     * @param databaseName database name
     * @return context key
     */
    public static PipelineContextKey buildForProxy(final String databaseName) {
        return new PipelineContextKey(databaseName, InstanceType.PROXY);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PipelineContextKey that = (PipelineContextKey) o;
        return instanceType == that.instanceType && Objects.equals(filterDatabaseName(this), filterDatabaseName(that));
    }
    
    private String filterDatabaseName(final PipelineContextKey contextKey) {
        return contextKey.getInstanceType() == InstanceType.PROXY ? "" : contextKey.getDatabaseName();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(instanceType, filterDatabaseName(this));
    }
}
