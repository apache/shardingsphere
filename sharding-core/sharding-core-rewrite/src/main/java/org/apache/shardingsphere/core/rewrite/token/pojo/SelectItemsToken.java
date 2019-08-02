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

package org.apache.shardingsphere.core.rewrite.token.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;

/**
 * Select items token.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public final class SelectItemsToken extends SQLToken implements Attachable {
    
    private final Collection<String> items;
    
    public SelectItemsToken(final int startIndex, final Collection<String> items) {
        super(startIndex);
        this.items = items;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String item : items) {
            result.append(", ");
            result.append(item);
        }
        return result.toString();
    }
}
