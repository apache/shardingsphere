#Requires -Version 7.4

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

# This file is only used in the PowerShell 7 of ShardingSphere in GitHub Actions environment and should not be executed manually in a development environment.
# Background information can be found at https://github.com/apache/shardingsphere/pull/35905 .
iex "& { $(irm https://raw.githubusercontent.com/microsoft/Windows-Containers/refs/heads/Main/helpful_tools/Install-DockerCE/uninstall-docker-ce.ps1) } -Force"
winget install --id jazzdelightsme.WingetPathUpdater --source winget
winget install --id SUSE.RancherDesktop --source winget --skip-dependencies
rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false
$deadline = (Get-Date).AddMinutes(10)
$state = "UNKNOWN"
while ((Get-Date) -lt $deadline)
{
    $now = Get-Date
    $deadlineString = $deadline.ToString("u")
    Write-Host "Waiting for backend: ($state) $now / $deadlineString"
    $rdProcess = Get-Process -Name "Rancher Desktop" -ErrorAction SilentlyContinue | Sort-Object -Property StartTime | Select-Object -First 1
    if (-not $rdProcess)
    {
        $state = "NOT_RUNNING"
        Start-Sleep -Seconds 10
        continue
    }
    $rdEngineJsonPath = Join-Path $env:LOCALAPPDATA "rancher-desktop\rd-engine.json"
    if (-not (Test-Path $rdEngineJsonPath))
    {
        $state = "NO_SERVER_CONFIG"
        Start-Sleep -Seconds 10
        continue
    }
    try
    {
        $state = (rdctl api /v1/backend_state | ConvertFrom-Json).vmState
    }
    catch
    {
        $state = "NO_RESPONSE"
    }
    switch ($state)
    {
        "ERROR" {
            Write-Error "Backend reached error state."
            exit 1
        }
        "STARTED" {
            Write-Host "PID $( $rdProcess.Id ) has reached state $state, accepting"
            exit 0
        }
        "DISABLED" {
            Write-Host "PID $( $rdProcess.Id ) has reached state $state, accepting"
            exit 0
        }
        default {
            Write-Host "Backend state: $state"
        }
    }
    Start-Sleep -Seconds 10
}
Write-Error "Timed out waiting for backend to stabilize."
Write-Error "Current time: $( Get-Date )"
Write-Error "Deadline: $($deadline.ToString("u") )"
exit 1
