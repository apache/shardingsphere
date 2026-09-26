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

package org.apache.shardingsphere.globalclock.distsql.handler;

import org.apache.shardingsphere.distsql.handler.executor.export.ExportedSnapshotInfo;
import org.apache.shardingsphere.distsql.handler.executor.export.ExportedSnapshotInfoGenerator;
import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Generates GlobalClock snapshot information for exported cluster metadata.
 */
public final class GlobalClockExportedSnapshotInfoGenerator implements ExportedSnapshotInfoGenerator {
    
    @Override
    public Optional<ExportedSnapshotInfo> generate(final ShardingSphereMetaData metaData) {
        GlobalClockRule globalClockRule = metaData.getGlobalRuleMetaData().getSingleRule(GlobalClockRule.class);
        if (!globalClockRule.getConfiguration().isEnabled()) {
            return Optional.empty();
        }
        ExportedSnapshotInfo result = new ExportedSnapshotInfo();
        result.setCsn(String.valueOf(globalClockRule.getGlobalClockProvider().map(GlobalClockProvider::getCurrentTimestamp).orElse(0L)));
        result.setCreateTime(LocalDateTime.now());
        return Optional.of(result);
    }
}
