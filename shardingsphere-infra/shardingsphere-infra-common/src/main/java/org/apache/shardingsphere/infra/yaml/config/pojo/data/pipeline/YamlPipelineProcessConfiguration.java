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

package org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;

/**
 * YAML pipeline process configuration.
 */
@Getter
@Setter
@ToString
public final class YamlPipelineProcessConfiguration implements YamlConfiguration {
    
    private YamlPipelineReadConfiguration read;
    
    private YamlPipelineWriteConfiguration write;
    
    private YamlAlgorithmConfiguration streamChannel;
    
    /**
     * Copy non-null fields from another.
     *
     * @param another another configuration
     */
    // TODO add unit test
    public void copyNonNullFields(final YamlPipelineProcessConfiguration another) {
        if (null == read) {
            read = another.getRead();
        } else {
            read.copyNonNullFields(another.getRead());
        }
        if (null == write) {
            write = another.getWrite();
        } else {
            write.copyNonNullFields(another.getWrite());
        }
        if (null == streamChannel) {
            streamChannel = another.getStreamChannel();
        }
    }
}
