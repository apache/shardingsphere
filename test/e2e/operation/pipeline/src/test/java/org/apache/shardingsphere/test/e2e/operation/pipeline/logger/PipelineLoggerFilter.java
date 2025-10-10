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

package org.apache.shardingsphere.test.e2e.operation.pipeline.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.base.Strings;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class PipelineLoggerFilter extends Filter<ILoggingEvent> {
    
    private static final Set<String> IGNORED_LOGGER_NAMES = getIgnoredLoggerNames();
    
    private static final Map<String, String[]> IGNORED_PROXY_LOG_ARGS = getIgnoredProxyLogArgs();
    
    private static Set<String> getIgnoredLoggerNames() {
        Set<String> result = new HashSet<>();
        result.add(":zookeeper");
        result.add(":mysql");
        result.add(":postgresql");
        result.add(":opengauss");
        return result;
    }
    
    private static Map<String, String[]> getIgnoredProxyLogArgs() {
        Map<String, String[]> result = new LinkedHashMap<>();
        result.put("atomikos", new String[]{"- tips & advice", "- working demos", "- access to the full documentation", "- special exclusive bonus offers not available to others"});
        return result;
    }
    
    @Override
    public FilterReply decide(final ILoggingEvent event) {
        if (IGNORED_LOGGER_NAMES.contains(event.getLoggerName())) {
            return FilterReply.DENY;
        }
        if ((":" + ProxyContainerConstants.PROXY_CONTAINER_NAME_PREFIX).equals(event.getLoggerName())) {
            for (Object each : event.getArgumentArray()) {
                String arg = each.toString();
                if (Strings.isNullOrEmpty(arg)) {
                    continue;
                }
                for (Entry<String, String[]> entry : IGNORED_PROXY_LOG_ARGS.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(arg) || Arrays.stream(entry.getValue()).anyMatch(arg::contains)) {
                        return FilterReply.DENY;
                    }
                }
            }
        }
        return FilterReply.NEUTRAL;
    }
}
