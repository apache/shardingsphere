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

package org.apache.shardingsphere.scaling.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.infra.distsql.update.RALUpdater;
import org.apache.shardingsphere.scaling.distsql.statement.RestoreScalingSourceWritingStatement;

/**
 * Restore scaling source writing updater.
 */
public final class RestoreScalingSourceWritingUpdater implements RALUpdater<RestoreScalingSourceWritingStatement> {
    
    private static final RuleAlteredJobAPI RULE_ALTERED_JOB_API = RuleAlteredJobAPIFactory.getInstance();
    
    @Override
    public void executeUpdate(final RestoreScalingSourceWritingStatement sqlStatement) {
        RULE_ALTERED_JOB_API.restoreClusterWriteDB(sqlStatement.getJobId());
    }
    
    @Override
    public String getType() {
        return RestoreScalingSourceWritingStatement.class.getName();
    }
}
