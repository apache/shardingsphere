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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowSynchronizationSupportTest {
    
    @Test
    void assertSynchronize() {
        new WorkflowSynchronizationSupport(Duration.ofMillis(1L), Duration.ofMillis(1L))
                .synchronize(() -> createValidationReport(WorkflowLifecycle.STATUS_PASSED));
    }
    
    @Test
    void assertSynchronizeAfterPolling() {
        AtomicInteger attempts = new AtomicInteger();
        new WorkflowSynchronizationSupport(Duration.ofMillis(2L), Duration.ofMillis(1L)).synchronize(
                () -> createValidationReport(attempts.incrementAndGet() > 1 ? WorkflowLifecycle.STATUS_PASSED : WorkflowLifecycle.STATUS_FAILED));
        assertThat(attempts.get(), is(2));
    }
    
    @Test
    void assertSynchronizeWhenValidationFails() {
        ValidationReport validationReport = createValidationReport(WorkflowLifecycle.STATUS_FAILED);
        validationReport.getMismatches().add(Map.of("code", WorkflowIssueCode.RULE_STATE_MISMATCH,
                "impact", "Rule metadata is not visible from Proxy DistSQL."));
        AtomicInteger attempts = new AtomicInteger();
        WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport(Duration.ofNanos(3L), Duration.ofNanos(1L));
        WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class,
                () -> workflowSynchronizationSupport.synchronize(() -> {
                    attempts.incrementAndGet();
                    return validationReport;
                }));
        assertThat(actual.getIssueCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
        assertThat(actual.getMessage(), is("Rule metadata is not visible from Proxy DistSQL."));
        assertThat(attempts.get(), is(3));
    }
    
    @Test
    void assertSynchronizeWhenQueryFails() {
        AtomicInteger attempts = new AtomicInteger();
        IllegalStateException expected = new IllegalStateException("Query failed.");
        WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport(Duration.ofMillis(2L), Duration.ofMillis(1L));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> workflowSynchronizationSupport.synchronize(() -> {
            attempts.incrementAndGet();
            throw expected;
        }));
        assertThat(actual, sameInstance(expected));
        assertThat(attempts.get(), is(1));
    }
    
    @Test
    void assertSynchronizeWhenInterrupted() {
        WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport(Duration.ofMillis(2L), Duration.ofMillis(1L));
        Thread.currentThread().interrupt();
        try {
            WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class,
                    () -> workflowSynchronizationSupport.synchronize(() -> createValidationReport(WorkflowLifecycle.STATUS_FAILED)));
            assertThat(actual.getIssueCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
            assertThat(actual.getMessage(), is("Workflow synchronization was interrupted."));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
    
    @Test
    void assertInvalidSynchronizationWindow() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new WorkflowSynchronizationSupport(Duration.ZERO, Duration.ofMillis(1L)));
        assertThat(actual.getMessage(), is("Synchronization window must be positive."));
    }
    
    @Test
    void assertInvalidPollInterval() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new WorkflowSynchronizationSupport(Duration.ofMillis(1L), Duration.ZERO));
        assertThat(actual.getMessage(), is("Poll interval must be positive."));
    }
    
    private ValidationReport createValidationReport(final String overallStatus) {
        ValidationReport result = new ValidationReport();
        result.setOverallStatus(overallStatus);
        return result;
    }
}
