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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Workflow synchronization support.
 */
public final class WorkflowSynchronizationSupport {
    
    private static final int DEFAULT_MAX_ATTEMPTS = 30;
    
    private static final long DEFAULT_POLL_INTERVAL_MILLIS = 1000L;
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final int maxAttempts;
    
    private final long pollIntervalMillis;
    
    public WorkflowSynchronizationSupport() {
        maxAttempts = DEFAULT_MAX_ATTEMPTS;
        pollIntervalMillis = DEFAULT_POLL_INTERVAL_MILLIS;
    }
    
    public WorkflowSynchronizationSupport(final int maxAttempts, final long pollIntervalMillis) {
        this.maxAttempts = Math.max(maxAttempts, 1);
        this.pollIntervalMillis = Math.max(pollIntervalMillis, 0L);
    }
    
    /**
     * Synchronize workflow state by polling validation.
     *
     * @param validationReportSupplier validation report supplier
     */
    public void synchronize(final Supplier<ValidationReport> validationReportSupplier) {
        ValidationReport validationReport = null;
        for (int i = 0; i < maxAttempts; i++) {
            validationReport = validationReportSupplier.get();
            if (WorkflowLifecycle.STATUS_PASSED.equals(validationReport.getOverallStatus())) {
                return;
            }
            if (i < maxAttempts - 1) {
                sleep();
            }
        }
        throw createSynchronizationException(validationReport);
    }
    
    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(pollIntervalMillis);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new WorkflowSynchronizationException(WorkflowIssueCode.RULE_STATE_MISMATCH, "Workflow synchronization was interrupted.", List.of());
        }
    }
    
    private WorkflowSynchronizationException createSynchronizationException(final ValidationReport validationReport) {
        if (null == validationReport) {
            return new WorkflowSynchronizationException(WorkflowIssueCode.RULE_STATE_MISMATCH,
                    "Workflow execution completed before the resulting state became visible to Proxy validation.", List.of());
        }
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
