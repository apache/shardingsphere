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

package info.avalon566.shardingscaling.job.config;

import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Sync configuration.
 * @author avalon566
 */
@Data
@AllArgsConstructor
public class SyncConfiguration {

    private SyncType syncType;

    /**
     * 单表写入并发度.
     */
    private int concurrency;

    private RdbmsConfiguration readerConfiguration;

    private RdbmsConfiguration writerConfiguration;
}
