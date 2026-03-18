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

package org.apache.shardingsphere.shadow.route.retriever.dml.table.column.fixture;

import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowColumnDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;

public final class ShadowColumnDataSourceMappingsRetrieverFixture extends ShadowColumnDataSourceMappingsRetriever {
    
    public ShadowColumnDataSourceMappingsRetrieverFixture() {
        super(ShadowOperationType.SELECT);
    }
    
    @Override
    protected Collection<ShadowColumnCondition> getShadowColumnConditions(final String shadowColumnName) {
        return Collections.singleton(mock(ShadowColumnCondition.class));
    }
}
