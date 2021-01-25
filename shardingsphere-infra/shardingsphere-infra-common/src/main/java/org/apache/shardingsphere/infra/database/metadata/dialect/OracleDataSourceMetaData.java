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

package org.apache.shardingsphere.infra.database.metadata.dialect;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.UnrecognizedDatabaseURLException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data for Oracle.
 */
@Getter
public final class OracleDataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = 1521;
    
    private static final int THIN_MATCH_GROUP_COUNT = 5;
    
    private final String hostName;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    private final Pattern thinUrlPattern = Pattern.compile("jdbc:oracle:(thin|oci|kprb):@(//)?([\\w\\-\\.]+):?([0-9]*)[:/]([\\w\\-]+)", Pattern.CASE_INSENSITIVE);
    
    private final Pattern connectDescriptorUrlPattern = Pattern.compile("jdbc:oracle:(thin|oci|kprb):@[(\\w\\s=)]+HOST\\s*=\\s*(\\d+(\\."
            + "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}).*PORT\\s*=\\s*(\\d+).*SERVICE_NAME\\s*=\\s*(\\w+)\\)");
    
    public OracleDataSourceMetaData(final String url, final String username) {
        List<Matcher> matcherList = Arrays.asList(thinUrlPattern.matcher(url), connectDescriptorUrlPattern.matcher(url));
        Optional<Matcher> matcherOptional = matcherList.stream().filter(Matcher::find).findAny();
        if (!matcherOptional.isPresent()) {
            throw new UnrecognizedDatabaseURLException(url, thinUrlPattern.pattern());
        }
        Matcher matcher = matcherOptional.get();
        int groupCount = matcher.groupCount();
        if (THIN_MATCH_GROUP_COUNT == groupCount) {
            hostName = matcher.group(3);
            port = Strings.isNullOrEmpty(matcher.group(4)) ? DEFAULT_PORT : Integer.parseInt(matcher.group(4));
            catalog = matcher.group(5);
            schema = username;
        } else {
            hostName = matcher.group(2);
            port = Integer.parseInt(matcher.group(7));
            catalog = matcher.group(8);
            schema = username;
        }
    }
}
