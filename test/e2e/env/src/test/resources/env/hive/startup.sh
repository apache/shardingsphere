#!/bin/bash
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

# Start Hive services
start-hive.sh &

# Wait for HiveServer2 to be ready
echo "Waiting for HiveServer2 to start..."
sleep 60

# Execute initialization scripts
echo "Executing initialization scripts..."

# Create databases using beeline
beeline -u "jdbc:hive2://localhost:10000/default" -e "
CREATE DATABASE IF NOT EXISTS default;
CREATE DATABASE IF NOT EXISTS test_user;
CREATE DATABASE IF NOT EXISTS encrypt;
CREATE DATABASE IF NOT EXISTS expected_dataset;
"

# Execute actual init script for encrypt database
if [ -f "/docker-entrypoint-initdb.d/01-actual-init.sql" ]; then
    echo "Executing 01-actual-init.sql..."
    beeline -u "jdbc:hive2://localhost:10000/encrypt" -f "/docker-entrypoint-initdb.d/01-actual-init.sql"
fi

# Execute expected init script for expected_dataset database
if [ -f "/docker-entrypoint-initdb.d/01-expected-init.sql" ]; then
    echo "Executing 01-expected-init.sql..."
    beeline -u "jdbc:hive2://localhost:10000/expected_dataset" -f "/docker-entrypoint-initdb.d/01-expected-init.sql"
fi

echo "Initialization complete!"

# Keep container running
tail -f /dev/null