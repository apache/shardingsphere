/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.keygen.self.time;

/**
 * 时钟定义.
 * 
 * @author gaohongtao
 */
public abstract class AbstractClock {
    
    /**
     * 创建系统时钟.
     * 
     * @return 系统时钟
     */
    public static AbstractClock systemClock() {
        return new SystemClock();
    }
    
    /**
     * 返回从纪元开始的毫秒数.
     * 
     * @return 从纪元开始的毫秒数
     */
    public abstract long millis();
    
    private static final class SystemClock extends AbstractClock {
    
        @Override
        public long millis() {
            return System.currentTimeMillis();
        }
    }
}
