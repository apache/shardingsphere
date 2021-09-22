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

package org.apache.shardingsphere.scaling.core.api.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.api.SingleTableDataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.job.JobContext;

/**
 * Abstract single table data consistency checker.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class AbstractSingleTableDataConsistencyChecker implements SingleTableDataConsistencyChecker {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    private final JobContext jobContext;
    
    protected final DataSourceWrapper getSourceDataSource() {
        return dataSourceFactory.newInstance(jobContext.getJobConfig().getRuleConfig().getSource().unwrap());
    }
    
    protected final DataSourceWrapper getTargetDataSource() {
        return dataSourceFactory.newInstance(jobContext.getJobConfig().getRuleConfig().getTarget().unwrap());
    }
}
