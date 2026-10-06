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

package org.apache.shardingsphere.distsql.handler.executor.spi.update.rule.database;

import org.apache.shardingsphere.distsql.handler.executor.spi.fixture.FixtureRule;
import org.apache.shardingsphere.distsql.handler.executor.spi.update.rule.database.fixture.FixtureDatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

class DatabaseRuleDefinitionExecutorTest {
    
    private static final int THREAD_COUNT = 2;
    
    private static final int TIMEOUT_SECONDS = 10;
    
    @Test
    void assertConcurrentIsolation() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            Future<DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule>> firstFuture = executorService.submit(createTask(barrier, new FixtureRule("first")));
            Future<DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule>> secondFuture = executorService.submit(createTask(barrier, new FixtureRule("second")));
            DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule> actualFirst = firstFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule> actualSecond = secondFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertThat(actualFirst, not(sameInstance(actualSecond)));
        } finally {
            executorService.shutdownNow();
        }
    }
    
    @SuppressWarnings("unchecked")
    private Callable<DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule>> createTask(final CyclicBarrier barrier, final FixtureRule expectedRule) {
        return () -> {
            DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule> result =
                    TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, FixtureDatabaseRuleDefinitionStatement.class);
            result.setDatabase(null);
            result.setRule(expectedRule);
            barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            result.checkBeforeUpdate(new FixtureDatabaseRuleDefinitionStatement(expectedRule));
            return result;
        };
    }
}
