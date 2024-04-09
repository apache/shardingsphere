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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;

import java.util.Map;
import java.util.Properties;

/**
 * Pipeline data consistency checker.
 */
public interface PipelineDataConsistencyChecker extends PipelineCancellable {
    
    /**
     * Data consistency check.
     *
     * @param algorithmType algorithm type of {@link org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker}
     * @param algorithmProps algorithm properties of {@link org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker}
     * @return check results. key is logic table name, value is check result.
     */
    Map<String, TableDataConsistencyCheckResult> check(String algorithmType, Properties algorithmProps);
}
