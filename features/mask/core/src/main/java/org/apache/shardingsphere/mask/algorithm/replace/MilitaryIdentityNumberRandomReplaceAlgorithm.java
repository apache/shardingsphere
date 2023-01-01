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

package org.apache.shardingsphere.mask.algorithm.replace;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmUtil;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.Properties;
import java.util.Random;

/**
 * Military identity number random replace algorithm.
 */
public final class MilitaryIdentityNumberRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String TYPE_CODE = "type-codes";
    
    private Collection<String> typeCodes;
    
    @Getter
    private Properties props;
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        Random random = new Random();
        char[] chars = result.toCharArray();
        for (final String each : typeCodes) {
            if (result.startsWith(each)) {
                for (int i = each.length(); i < chars.length; i++) {
                    chars[i] = Character.forDigit(random.nextInt(10), 10);
                }
                break;
            }
        }
        return new String(chars);
    }
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.typeCodes = createTypeCodes(props);
    }
    
    private Collection<String> createTypeCodes(final Properties props) {
        MaskAlgorithmUtil.checkAtLeastOneCharConfig(props, TYPE_CODE, getType());
        return Splitter.on(",").trimResults().splitToList(props.getProperty(TYPE_CODE));
    }
    
    @Override
    public String getType() {
        return "MILITARY_IDENTITY_NUMBER_RANDOM_REPLACE";
    }
}
