<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Requirements Checklist: MCP LLM Docker E2E Environment Hardening

**Purpose**: Validate the Speckit package before implementation starts.
**Created**: 2026-05-17
**Package**: `.specify/specs/024-mcp-github-actions-e2e-hardening/`

## Governance

- [x] Active branch confirmed as `001-shardingsphere-mcp` without switching.
- [x] Package created manually without branch-changing Speckit commands.
- [x] Documentation-only work performed before implementation authorization.
- [x] Existing worktree changes preserved and not reverted.
- [x] Repository rules from `AGENTS.md` and `CODE_OF_CONDUCT.md` considered.
- [x] Destructive Docker cleanup identified as confirmation-gated.

## Requirement Quality

- [x] LLM workflow trigger policy is explicit: MCP-scoped PR plus manual plus scheduled, non-required for merge.
- [x] LLM workflow path filters exclude broad root, aggregator, and Speckit-only path triggers.
- [x] Dedicated LLM workflow files are captured as the only non-module lane-infrastructure trigger exception.
- [x] GitHub path-filter changed-file limits and manual dispatch fallback are captured.
- [x] Local architecture-aware image build is required and testable.
- [x] Local helper dry-run or print-mode validation is required to avoid unnecessary model downloads during script checks.
- [x] Local helper shell portability, license header, and execution style are captured.
- [x] Docker BuildKit/GHA cache requirement preserves checksum-based model integrity.
- [x] Docker cache export failure risk is separated from score-evidence failure.
- [x] Buildx local image loading is constrained to a single-platform output.
- [x] Docker GHA cache API v2 compatibility diagnostics are captured.
- [x] Docker preflight and image build are required before Maven install for fail-fast behavior.
- [x] Docker cleanup is split into low-risk dangling image/build-cache cleanup and higher-risk volume pruning.
- [x] Model, quantization, checksum, Docker-owned runtime, and artifact metadata must stay unchanged.
- [x] README and README_ZH alignment is required.

## Testability

- [x] Workflow trigger scope has an `rg pull_request|paths` verification.
- [x] Broad path trigger removal has a reverse-search verification.
- [x] Local helper behavior has architecture and image tag checks.
- [x] Local helper no-build validation is testable.
- [x] Local helper syntax and README execution style are testable.
- [x] Build cache can be reviewed through workflow YAML and Buildx configuration.
- [x] Explicit Buildx `context`, `file`, `load`, and cache inputs are testable by YAML inspection.
- [x] Absence of release-style multi-platform output in the LLM runtime build is testable by YAML inspection.
- [x] Buildx version logging is testable by workflow inspection.
- [x] Docker action major-version stability is captured to avoid accidental dependency upgrades.
- [x] Step order can be reviewed statically.
- [x] Focused LLM configuration/runtime tests are identified.
- [x] Scoped Checkstyle/Spotless verification is required for touched modules.
- [x] `mcp-builder` review is required when implementation touches MCP or MCP E2E files.

## Confirmed Decisions Before Code

- [x] Do not switch branches.
- [x] Do not edit implementation code in this Speckit-only round.
- [x] Keep `llama.cpp` plus `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- [x] Do not publish a prebuilt image in this slice.
- [x] Do not prune Docker volumes unless the user explicitly confirms that higher-risk cleanup.
