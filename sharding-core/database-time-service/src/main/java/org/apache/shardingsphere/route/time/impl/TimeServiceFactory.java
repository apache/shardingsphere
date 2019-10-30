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

package org.apache.shardingsphere.route.time.impl;

import org.apache.shardingsphere.core.route.spi.TimeService;
import org.apache.shardingsphere.route.time.TimeServiceConfig;
import org.apache.shardingsphere.route.time.spi.SPIDataBaseSQLEntry;

/**
 * A Factory of creating TimeService.
 *
 * @author chenchuangliu
 */
public final class TimeServiceFactory {

    /**
     * create a TimeService by {@link TimeServiceConfig}.
     *
     * @return TimeService
     */
    public static TimeService createTimeService() {
        TimeServiceConfig config = TimeServiceConfig.getInstance();
        return new DatabaseTimeService(config.getDataSource(), new SPIDataBaseSQLEntry(config.getDriverClassName()).getSQL());
    }
}
