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

package org.apache.shardingsphere.integration.data.pipeline.framework.watcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.NativeComposedContainer;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class ScalingWatcher extends TestWatcher {
    
    private final BaseComposedContainer composedContainer;
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    protected void failed(final Throwable e, final Description description) {
        if (composedContainer instanceof NativeComposedContainer) {
            super.failed(e, description);
            return;
        }
        List<Map<String, Object>> previewList = jdbcTemplate.queryForList("preview select * from t_order");
        List<Map<String, Object>> shardingAlgorithms = jdbcTemplate.queryForList("SHOW SHARDING ALGORITHMS");
        log.warn("watcher failed, preview:{}, shardingAlgorithms:{}", previewList, shardingAlgorithms);
    }
    
    @Override
    protected void finished(final Description description) {
        composedContainer.close();
    }
}
