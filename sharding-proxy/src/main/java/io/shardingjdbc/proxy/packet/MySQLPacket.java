/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.packet;

import lombok.Getter;
import lombok.Setter;

/**
 * MySQL packet.
 *
 * @author zhangliang
 */
@Getter
@Setter
public abstract class MySQLPacket {
    
    public static final int PAYLOAD_LENGTH = 3;
    
    public static final int SEQUENCE_LENGTH = 1;
    
    private int sequenceId;
}
