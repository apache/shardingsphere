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

package org.apache.shardingsphere.mode.manager.cluster.process;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;

@Getter
public final class GovernanceExecuteProcessReporterSubscriberFixture {
    
    private String value = "";
    
    public GovernanceExecuteProcessReporterSubscriberFixture() {
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Fired on execute process summary report event received.
     * 
     * @param executeProcessSummaryReportEvent execute process summary report event
     */
    @Subscribe
    public void onExecuteProcessSummaryReportEvent(final ExecuteProcessSummaryReportEvent executeProcessSummaryReportEvent) {
        value = executeProcessSummaryReportEvent.getExecuteProcessContext().getExecutionID();
    }
    
    /**
     * Fired on execute process unit report event.
     * 
     * @param executeProcessUnitReportEvent execute process unit report event
     */
    @Subscribe
    public void onExecuteProcessUnitReportEvent(final ExecuteProcessUnitReportEvent executeProcessUnitReportEvent) {
        value = executeProcessUnitReportEvent.getExecutionID();
    }
    
    /**
     * Fired on execute process report event.
     * 
     * @param executeProcessReportEvent execute process report event
     */
    @Subscribe
    public void onExecuteProcessReportEvent(final ExecuteProcessReportEvent executeProcessReportEvent) {
        value = executeProcessReportEvent.getExecutionID();
    }
}
