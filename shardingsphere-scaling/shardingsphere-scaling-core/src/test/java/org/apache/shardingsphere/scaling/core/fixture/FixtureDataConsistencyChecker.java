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

package org.apache.shardingsphere.scaling.core.fixture;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.scaling.core.execute.executor.sqlbuilder.ScalingSQLBuilder;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.AbstractDataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;

import java.util.Collections;
import java.util.Map;

public final class FixtureDataConsistencyChecker extends AbstractDataConsistencyChecker implements DataConsistencyChecker {
    
    public FixtureDataConsistencyChecker(final ScalingJob scalingJob) {
        super(scalingJob);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> countCheck() {
        return super.countCheck();
    }
    
    @Override
    public Map<String, Boolean> dataCheck() {
        return Collections.emptyMap();
    }
    
    @Override
    protected ScalingSQLBuilder getSqlBuilder() {
        return new FixtureScalingSQLBuilder(Maps.newHashMap());
    }
}
