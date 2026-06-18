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

package org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.yaml.YamlTableCheckRangePosition;

import java.util.List;

/**
 * Yaml data consistency check job item progress.
 */
@Getter
@Setter
public final class YamlConsistencyCheckJobItemProgress implements YamlPipelineJobItemProgressConfiguration {
    
    private String status;
    
    private String tableNames;
    
    private String ignoredTableNames;
    
    private Long checkedRecordsCount;
    
    private Long recordsCount;
    
    private Long checkBeginTimeMillis;
    
    private Long checkEndTimeMillis;
    
    private List<YamlTableCheckRangePosition> tableCheckRangePositions;
    
    private String sourceDatabaseType;
}
