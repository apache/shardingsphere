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

import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilder;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.check.consistency.AbstractDataConsistencyChecker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FixtureDataConsistencyChecker extends AbstractDataConsistencyChecker {
    
    public FixtureDataConsistencyChecker(final JobContext jobContext) {
        super(jobContext);
    }
    
    @Override
    public Map<String, Boolean> dataCheck() {
        return Collections.emptyMap();
    }
    
    @Override
    protected ScalingSQLBuilder getSQLBuilder() {
        return new FixtureSQLBuilder(new HashMap<>());
    }
}
