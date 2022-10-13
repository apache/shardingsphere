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

package org.apache.shardingsphere.integration.data.pipeline.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class ScalingLoggerFilter extends Filter<ILoggingEvent> {
    
    private static final List<String> IGNORE_LOGGER_NAMES = Arrays.asList(":mysql", ":zookeeper", ":postgresql", ":opengauss");
    
    private static final String[] IGNORE_ATOMIKOS_ARGS = new String[]{"- tips & advice", "- working demos", "- access to the full documentation",
            "- special exclusive bonus offers not available to others"};
    
    @Override
    public FilterReply decide(final ILoggingEvent event) {
        if (IGNORE_LOGGER_NAMES.contains(event.getLoggerName())) {
            return FilterReply.DENY;
        }
        if (":Scaling-Proxy".equals(event.getLoggerName())) {
            for (Object each : event.getArgumentArray()) {
                String arg = each.toString();
                if (Strings.isNullOrEmpty(arg)) {
                    continue;
                }
                if ("atomikos".equalsIgnoreCase(arg) || StringUtils.containsAny(arg, IGNORE_ATOMIKOS_ARGS)) {
                    return FilterReply.DENY;
                }
            }
        }
        return FilterReply.NEUTRAL;
    }
}
