/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * Select items token.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter
@ToString
public final class ItemsToken extends SQLToken {
    
    @Setter
    private boolean isFirstOfItemsSpecial;
    
    private final List<String> items = new LinkedList<>();
    
    public ItemsToken(final int beginPosition) {
        super(beginPosition);
    }
}
