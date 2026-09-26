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

package org.apache.shardingsphere.proxy.backend.handler.distsql;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DistSQLExecutorSPITest {
    
    @Test
    void assertMigratedQueryExecutorsLoadOncePerStatement() {
        Collection<DistSQLQueryExecutor> executors = StreamSupport.stream(ServiceLoader.load(DistSQLQueryExecutor.class).spliterator(), false)
                .filter(each -> isMigratedExecutor(each.getClass().getName())).collect(Collectors.toList());
        assertThat(executors.size(), is(12));
        assertThat(executors.stream().map(DistSQLQueryExecutor::getType).distinct().count(), is(12L));
    }
    
    @Test
    void assertMigratedUpdateExecutorsLoadOncePerStatement() {
        Collection<DistSQLUpdateExecutor> executors = StreamSupport.stream(ServiceLoader.load(DistSQLUpdateExecutor.class).spliterator(), false)
                .filter(each -> isMigratedExecutor(each.getClass().getName())).collect(Collectors.toList());
        assertThat(executors.size(), is(8));
        assertThat(executors.stream().map(DistSQLUpdateExecutor::getType).distinct().count(), is(8L));
    }
    
    private boolean isMigratedExecutor(final String className) {
        return className.startsWith("org.apache.shardingsphere.distsql.handler.executor.ral.queryable.")
                || className.startsWith("org.apache.shardingsphere.distsql.handler.executor.ral.updatable.")
                || className.startsWith("org.apache.shardingsphere.distsql.handler.executor.rul.")
                || "org.apache.shardingsphere.parser.distsql.handler.query.ParseDistSQLExecutor".equals(className);
    }
}
