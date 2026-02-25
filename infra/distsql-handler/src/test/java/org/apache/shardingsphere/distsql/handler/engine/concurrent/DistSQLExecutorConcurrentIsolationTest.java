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

package org.apache.shardingsphere.distsql.handler.engine.concurrent;

import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.concurrent.fixture.FixtureDatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.distsql.handler.engine.concurrent.fixture.FixtureDistSQLQueryStatement;
import org.apache.shardingsphere.distsql.handler.engine.concurrent.fixture.FixtureDistSQLUpdateStatement;
import org.apache.shardingsphere.distsql.handler.engine.concurrent.fixture.FixtureRule;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

class DistSQLExecutorConcurrentIsolationTest {
    
    private static final int THREAD_COUNT = 2;
    
    private static final int TIMEOUT_SECONDS = 10;
    
    @Test
    void assertDatabaseRuleDefinitionExecutorConcurrentIsolation() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        assertConcurrentIsolation(createDatabaseRuleDefinitionTask(barrier, new FixtureRule("first")), createDatabaseRuleDefinitionTask(barrier, new FixtureRule("second")));
    }
    
    @Test
    void assertDistSQLQueryExecutorConcurrentIsolation() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        assertConcurrentIsolation(createDistSQLQueryTask(barrier, new FixtureRule("first")), createDistSQLQueryTask(barrier, new FixtureRule("second")));
    }
    
    @Test
    void assertDistSQLUpdateExecutorConcurrentIsolation() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        assertConcurrentIsolation(createDistSQLUpdateTask(barrier, new FixtureRule("first")), createDistSQLUpdateTask(barrier, new FixtureRule("second")));
    }
    
    @SuppressWarnings("unchecked")
    private Callable<Object> createDatabaseRuleDefinitionTask(final CyclicBarrier barrier, final FixtureRule expectedRule) {
        return () -> {
            DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule> executor =
                    TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, FixtureDatabaseRuleDefinitionStatement.class);
            executor.setDatabase(null);
            executor.setRule(expectedRule);
            barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.checkBeforeUpdate(new FixtureDatabaseRuleDefinitionStatement(expectedRule));
            return executor;
        };
    }
    
    @SuppressWarnings("unchecked")
    private Callable<Object> createDistSQLQueryTask(final CyclicBarrier barrier, final FixtureRule expectedRule) {
        return () -> {
            DistSQLQueryExecutor<FixtureDistSQLQueryStatement> executor = TypedSPILoader.getService(DistSQLQueryExecutor.class, FixtureDistSQLQueryStatement.class);
            ((DistSQLExecutorRuleAware<ShardingSphereRule>) executor).setRule(expectedRule);
            barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.getColumnNames(new FixtureDistSQLQueryStatement(expectedRule));
            return executor;
        };
    }
    
    @SuppressWarnings("unchecked")
    private Callable<Object> createDistSQLUpdateTask(final CyclicBarrier barrier, final FixtureRule expectedRule) {
        return () -> {
            DistSQLUpdateExecutor<FixtureDistSQLUpdateStatement> executor = TypedSPILoader.getService(DistSQLUpdateExecutor.class, FixtureDistSQLUpdateStatement.class);
            ((DistSQLExecutorRuleAware<ShardingSphereRule>) executor).setRule(expectedRule);
            barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.executeUpdate(new FixtureDistSQLUpdateStatement(expectedRule), null);
            return executor;
        };
    }
    
    private void assertConcurrentIsolation(final Callable<Object> firstTask, final Callable<Object> secondTask) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            Future<Object> firstFuture = executorService.submit(firstTask);
            Future<Object> secondFuture = executorService.submit(secondTask);
            Object firstExecutor = firstFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Object secondExecutor = secondFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertThat(firstExecutor, not(sameInstance(secondExecutor)));
        } finally {
            executorService.shutdownNow();
        }
    }
}
