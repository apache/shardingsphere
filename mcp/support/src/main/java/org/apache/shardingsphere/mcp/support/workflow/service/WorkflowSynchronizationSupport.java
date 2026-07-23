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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Workflow synchronization support.
 */
public final class WorkflowSynchronizationSupport {
    
    /**
     * Default synchronization window.
     */
    public static final Duration DEFAULT_SYNCHRONIZATION_WINDOW = Duration.ofSeconds(30L);
    
    /**
     * Default interval between validation attempts.
     */
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1L);
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final long synchronizationWindowNanos;
    
    private final long pollIntervalNanos;
    
    /**
     * Constructs workflow synchronization support.
     *
     * @param synchronizationWindow synchronization window spent waiting between validation attempts
     * @param pollInterval interval between validation attempts
     * @throws IllegalArgumentException if either duration is not positive
     */
    public WorkflowSynchronizationSupport(final Duration synchronizationWindow, final Duration pollInterval) {
        ShardingSpherePreconditions.checkState(!synchronizationWindow.isZero() && !synchronizationWindow.isNegative(),
                () -> new IllegalArgumentException("Synchronization window must be positive."));
        ShardingSpherePreconditions.checkState(!pollInterval.isZero() && !pollInterval.isNegative(),
                () -> new IllegalArgumentException("Poll interval must be positive."));
        synchronizationWindowNanos = synchronizationWindow.toNanos();
        pollIntervalNanos = pollInterval.toNanos();
    }
    
    /**
     * Synchronize workflow state by polling validation. Time spent obtaining validation reports is excluded from the synchronization window.
     *
     * @param validationReportSupplier validation report supplier
     */
    public void synchronize(final Supplier<ValidationReport> validationReportSupplier) {
        ValidationReport validationReport;
        long remainingWaitNanos = synchronizationWindowNanos;
        while (true) {
            validationReport = validationReportSupplier.get();
            if (WorkflowLifecycle.STATUS_PASSED.equals(validationReport.getOverallStatus())) {
                return;
            }
            if (0L == remainingWaitNanos) {
                break;
            }
            long waitNanos = Math.min(remainingWaitNanos, pollIntervalNanos);
            waitForNextValidation(waitNanos);
            remainingWaitNanos -= waitNanos;
        }
        throw createSynchronizationException(validationReport);
    }
    
    private void waitForNextValidation(final long waitNanos) {
        try {
            TimeUnit.NANOSECONDS.sleep(waitNanos);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new WorkflowSynchronizationException(WorkflowIssueCode.RULE_STATE_MISMATCH, "Workflow synchronization was interrupted.", List.of());
        }
    }
    
    private WorkflowSynchronizationException createSynchronizationException(final ValidationReport validationReport) {
        return new WorkflowSynchronizationException(validationSupport.resolveValidationIssueCode(validationReport),
                resolveFailureMessage(validationReport), validationReport.getMismatches());
    }
    
    private String resolveFailureMessage(final ValidationReport validationReport) {
        for (Map<String, Object> each : validationReport.getMismatches()) {
            String impact = Objects.toString(each.get("impact"), "").trim();
            if (!impact.isEmpty()) {
                return impact;
            }
        }
        return "Workflow execution completed before the resulting state became visible to Proxy validation.";
    }
}
