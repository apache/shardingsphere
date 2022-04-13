/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public final class RuleAlteredJobWorkerTest {
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        RuleAlteredJobWorker.initWorkerIfNecessary();
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertCreateRuleAlteredContextNoAlteredRule() {
        JobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobConfig.setWorkflowConfig(new WorkflowConfiguration("logic_db", ImmutableMap.of(), 0, 1));
        RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
    }
    
    @Test
    public void assertCreateRuleAlteredContextSuccess() {
        JobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        final RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        Assert.assertNotNull(ruleAlteredContext.getOnRuleAlteredActionConfig());
    }
}
