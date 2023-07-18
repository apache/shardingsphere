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

package org.apache.shardingsphere.test.e2e.data.pipeline.framework.param;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;

import java.util.Arrays;

/**
 * Pipeline E2E condition.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineE2ECondition {
    
    /**
     * Judge whether pipeline E2E is enabled.
     * 
     * @param databaseTypes database types
     * @return enabled or not
     */
    public static boolean isEnabled(final DatabaseType... databaseTypes) {
        if (PipelineEnvTypeEnum.NONE == PipelineE2EEnvironment.getInstance().getItEnvType()) {
            return false;
        }
        return 0 == databaseTypes.length || Arrays.stream(databaseTypes).anyMatch(each -> !PipelineE2EEnvironment.getInstance().listStorageContainerImages(each).isEmpty());
    }
}
