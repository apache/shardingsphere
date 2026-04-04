# Tasks: ShardingSphere MCP Official Registry Publication and OCI Distribution

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-registry-publication/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`

**Tests**: Use scoped Maven package validation, JSON validation, workflow inspection, and static grep verification.

## Phase 1: Spec Freeze

- [X] T001 Add Speckit spec in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-registry-publication/spec.md`
- [X] T002 Record publication decisions in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-registry-publication/research.md`
- [X] T003 Freeze implementation plan in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-registry-publication/plan.md`

## Phase 2: Registry Metadata and Distribution Contract

- [ ] T004 Add official server metadata in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/server.json`
- [ ] T005 Add stdio image launch config in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/resources/conf/mcp-stdio.yaml`
- [ ] T006 Add Docker entrypoint routing in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/bin/docker-entrypoint.sh`
- [ ] T007 Update `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/Dockerfile`
  with MCP Registry verification labels and the new entrypoint
- [ ] T008 Update `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/pom.xml`
  so packaged scripts remain executable

## Phase 3: Release Automation

- [ ] T009 Add MCP release workflow in `/Users/zhangliang/IdeaProjects/shardingsphere/.github/workflows/mcp-build.yml`
- [ ] T010 Update `/Users/zhangliang/IdeaProjects/shardingsphere/.github/workflows/jdk17-subchain-ci.yml`
  with MCP Registry metadata validation

## Phase 4: Documentation

- [ ] T011 Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
  with Registry publication and stdio image usage
- [ ] T012 Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`
  with Registry publication and stdio image usage
- [ ] T013 Update `/Users/zhangliang/IdeaProjects/shardingsphere/docs/community/content/involved/release/shardingsphere.en.md`
  with MCP publication notes
- [ ] T014 Update `/Users/zhangliang/IdeaProjects/shardingsphere/docs/community/content/involved/release/shardingsphere.cn.md`
  with MCP publication notes

## Phase 5: Validation

- [ ] T015 Run scoped packaging validation for `distribution/mcp`
- [ ] T016 Run JSON validation for `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/server.json`
- [ ] T017 Run static grep verification for MCP Registry labels, env vars and workflow commands
