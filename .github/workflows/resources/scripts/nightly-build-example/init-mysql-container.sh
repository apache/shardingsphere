#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

if [ $# -ne 1 ]; then
    echo "Usage: $0 <init.sql path>"
    exit 1
fi

INIT_SQL_PATH=$1

if [ ! -f "$INIT_SQL_PATH" ]; then
    echo "Error: init.sql file not found at $INIT_SQL_PATH"
    exit 1
fi

echo "Executing init.sql: $INIT_SQL_PATH"
mysql -uroot -h127.0.0.1 -p123456 < "$INIT_SQL_PATH"
echo "init.sql execution completed."
