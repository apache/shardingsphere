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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule.type;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.manager.rule.DatabaseRuleItemManager;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereArgumentVerifyMatchers.deepEq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UniqueRuleItemConfigurationChangedHandlerTest {
    
    private UniqueRuleItemConfigurationChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        handler = new UniqueRuleItemConfigurationChangedHandler(contextManager);
    }
    
    @Test
    void assertHandleWithInvalidPath() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture", "foo", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).alter(any());
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).drop(any());
    }
    
    @Test
    void assertHandleWithIgnoreType() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/unique/active_version", "0", Type.IGNORED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).alter(any());
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).drop(any());
    }
    
    @Test
    void assertHandleWithAddItem() {
        MetaDataManagerPersistService persistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        DatabaseRuleItemManager ruleItemManager = contextManager.getMetaDataContextManager().getDatabaseRuleItemManager();
        doAnswer(invocation -> {
            assertTrue(Thread.holdsLock(persistService));
            return null;
        }).when(ruleItemManager).alter(any());
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/unique/active_version", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager())
                .alter(deepEq(new DatabaseRuleNodePath("foo_db", "fixture", new DatabaseRuleItem("unique"))));
    }
    
    @Test
    void assertHandleWithConcurrentCreate() throws InterruptedException, ExecutionException, TimeoutException {
        MetaDataManagerPersistService persistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        DatabaseRuleItemManager ruleItemManager = contextManager.getMetaDataContextManager().getDatabaseRuleItemManager();
        AtomicReference<String> actualMetaData = new AtomicReference<>("foo_before_create");
        CountDownLatch snapshotRead = new CountDownLatch(1);
        CountDownLatch createAttempted = new CountDownLatch(1);
        doAnswer(invocation -> {
            String previousMetaData = actualMetaData.get();
            snapshotRead.countDown();
            assertTrue(createAttempted.await(5L, TimeUnit.SECONDS));
            assertTrue(Thread.holdsLock(persistService));
            actualMetaData.set(previousMetaData);
            return null;
        }).when(ruleItemManager).alter(any());
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<?> ruleChange = executorService.submit(() -> handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/unique/active_version", "0", Type.UPDATED)));
            assertTrue(snapshotRead.await(5L, TimeUnit.SECONDS));
            Future<?> create = executorService.submit(() -> {
                createAttempted.countDown();
                synchronized (persistService) {
                    actualMetaData.set("foo_created_table");
                }
            });
            ruleChange.get(5L, TimeUnit.SECONDS);
            create.get(5L, TimeUnit.SECONDS);
            assertThat(actualMetaData.get(), is("foo_created_table"));
        } finally {
            createAttempted.countDown();
            executorService.shutdownNow();
        }
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager())
                .alter(deepEq(new DatabaseRuleNodePath("foo_db", "fixture", new DatabaseRuleItem("unique"))));
    }
    
    @Test
    void assertHandleWitDropItem() {
        MetaDataManagerPersistService persistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        DatabaseRuleItemManager ruleItemManager = contextManager.getMetaDataContextManager().getDatabaseRuleItemManager();
        doAnswer(invocation -> {
            assertTrue(Thread.holdsLock(persistService));
            return null;
        }).when(ruleItemManager).drop(any());
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/unique", "0", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager())
                .drop(deepEq(new DatabaseRuleNodePath("foo_db", "fixture", new DatabaseRuleItem("unique"))));
    }
}
