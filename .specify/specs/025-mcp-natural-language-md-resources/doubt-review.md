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

# Doubt Review: MCP Natural Language Markdown Resources

## Contract Under Review

The design must cover all phases for moving suitable MCP natural-language content to Markdown resources without changing production code in this round.
It must preserve MCP protocol behavior, keep structured metadata and machine contracts structured, and avoid branch switching.

## Doubt Cycle

- [x] Step 1: Claim recorded.
- [x] Step 2: Artifact and contract isolated.
- [x] Step 3: Adversarial review performed in-session against the design artifact.
- [x] Step 4: Findings reconciled into the spec and plan.
- [x] Step 5: Stop condition met because findings are addressed or documented as trade-offs.

Note: No fresh subagent or external CLI was invoked in this design-only round.
Cross-model review should be offered before code implementation if the user wants a second opinion on the final implementation plan.

## Claim

The phased plan safely moves MCP server instructions and other eligible authored prose to Markdown resources without turning structured descriptors,
runtime diagnostics, or protocol contracts into free-form Markdown.

Why this matters: a wrong boundary would either keep prompt prose hidden in Java constants or make MCP clients depend on unstructured text where deterministic contracts are required.

## Findings and Reconciliation

### Finding 1: "All phases" could be misread as permission for a bulk migration

Risk: the plan might encourage moving every English string in `mcp/**` into Markdown.

Reconciliation: the spec and plan now define phase gates and ownership classes.
Only authored prose can move to Markdown; descriptors, diagnostics, machine contracts, and structured dictionaries are explicit exclusions.

Status: addressed.

### Finding 2: Source-controlled Markdown license headers could leak into model context

Risk: new `server-instructions.md` should carry an ASF header, but `InitializeResult.instructions` should not expose license prose as model guidance.
Existing prompt Markdown resources show the same risk.

Reconciliation: the spec now requires model-facing Markdown loaders to strip source-control license headers before exposing text to MCP clients.
The plan adds loader tests for source-header stripping.

Status: addressed.

### Finding 3: Missing-resource fallback could hide packaging errors

Risk: a fallback to the old Java string would make tests pass while packaged resources are broken.

Reconciliation: the plan requires deterministic failure for missing, unreadable, or blank resources and forbids silent fallback to Java prompt prose.

Status: addressed.

### Finding 4: Server instructions could duplicate descriptor descriptions

Risk: moving prose to Markdown may create a second authoritative description of tools, resources, or prompts.

Reconciliation: the source map ties descriptor descriptions to YAML ownership.
The plan says server instructions must focus on discovery order, workflow relationships, constraints, and safety sequence, not full descriptor bodies.

Status: addressed.

### Finding 5: Structured workflow guidance might become hard to validate

Risk: `next_actions`, `recovery.next_actions`, workflow payload labels, and algorithm recommendations are natural language adjacent and could be moved into Markdown incorrectly.

Reconciliation: the ownership map classifies them as structured payload, machine contract, or structured metadata.
They remain outside Markdown unless future analysis proves a separate authored-prose layer exists.

Status: addressed.

### Finding 6: Public verification path for instructions may be awkward

Risk: `McpSyncServer` exposes server info and capabilities, but not an obvious public `instructions()` getter.
Reflection-only testing would be weaker and less aligned with public behavior.

Reconciliation: the plan prefers an initialize or wire-level assertion because MCP exposes instructions through initialization.
Reflection is only a fallback and must use `Plugins.getMemberAccessor()` if needed.

Status: addressed.

### Finding 7: Promoting a shared loader too early could overbuild the change

Risk: creating a broad support-level loader for one resource may be unnecessary.

Reconciliation: the plan leaves loader ownership open to implementation evidence.
It allows a local bootstrap loader first, or a shared support loader if prompt/header stripping is intentionally included.

Status: valid trade-off.

## Final Design State

The design is ready for review as a documentation package.
It should not proceed to code until the user explicitly authorizes implementation.

## Reanalysis Cycle 2

### Claim

The design remains correct after expanding from server instructions to all model-facing Markdown phases, including prompt-template header stripping and future discovery compatibility.

Why this matters: the broader plan could accidentally convert draft features into requirements or introduce a too-broad Markdown preprocessing rule.

### Finding 8: Latest protocol sources were not consistently used

Risk: citing `2025-06-18` prompt docs while code uses `2025-11-25` could hide version-specific prompt behavior.

Reconciliation: `source-map.md` now cites `2025-11-25` prompts, resources, and tools documentation.

Status: addressed.

### Finding 9: Draft discovery evidence could be mistaken for a current implementation requirement

Risk: treating `server/discover` as mandatory would expand this package beyond current ShardingSphere MCP behavior.

Reconciliation: `source-map.md` and `plan.md` now label discovery as draft-only future-direction evidence.
The only requirement is to reuse the same canonical instruction resource if discovery is implemented later.

Status: addressed.

### Finding 10: Header stripping rule was too broad

Risk: stripping "Markdown or YAML-style source headers" could remove intended content or imply descriptor YAML should pass through the same loader.

Reconciliation: `plan.md` now limits stripping to a leading ASF HTML comment block in Markdown resources.
Descriptor YAML remains outside model-facing Markdown loading.

Status: addressed.

### Finding 11: Prompt templates are already model-facing Markdown

Risk: only fixing server instructions would leave existing prompt templates exposing ASF headers if the current raw loader behavior is unchanged.

Reconciliation: the plan now includes prompt-template header stripping when implementation touches model-facing Markdown loading.
Placeholder rendering remains separate from generic Markdown loading.

Status: addressed.

### Finding 12: Instructions verification should not rely on SDK-private internals

Risk: `McpSyncServer` has no obvious public `instructions()` getter, tempting reflection-based tests.

Reconciliation: the plan now explicitly prefers HTTP and STDIO initialize-response assertions through wire behavior tests or a small test-helper hook.
Reflection remains fallback-only and must use `Plugins.getMemberAccessor()`.

Status: addressed.

### Finding 13: Header stripping could remove authored comments

Risk: removing the first HTML comment from every Markdown resource could delete intended model guidance if a document starts with a non-license comment.

Reconciliation: the plan now requires ASF license markers before stripping a leading HTML comment.
Tests must cover a non-license leading HTML comment that remains.

Status: addressed.

### Finding 14: Resource packaging was under-specified

Risk: module tests could pass while the MCP distribution omits the new instruction resource.

Reconciliation: the plan now includes a package-level resource check and `distribution/mcp` packaging command after implementation.

Status: addressed.

### Finding 15: Shared loader caching could mask classpath differences

Risk: a path-only static cache could return content from one class loader while tests or plugin-oriented runtime paths expect another resource view.

Reconciliation: the plan now avoids generic path-only global caching.
If future caching is needed, it must be keyed by both class loader and resource path with a documented reason.

Status: addressed.

### Finding 16: Header stripping could break prompt placeholder validation

Risk: `MCPDescriptorCatalogValidator` extracts placeholders from loaded prompt templates.
Changing the loader could accidentally alter placeholder extraction or rendering.

Reconciliation: the plan now requires placeholder extraction and rendering semantics to remain unchanged, with tests after header stripping.

Status: addressed.

### Finding 17: Markdown front matter would blur descriptor ownership

Risk: adding YAML front matter to Markdown resources would reintroduce descriptor-like metadata into prompt files.

Reconciliation: the spec and plan now exclude Markdown front matter.
Descriptor metadata remains in descriptor YAML.

Status: addressed.

### Finding 18: Server instructions could become a dynamic catalog

Risk: once moved to Markdown, the instructions file could grow into a duplicated tool/resource/prompt inventory or runtime database summary.

Reconciliation: the spec and plan now require instructions to stay static and concise.
Runtime metadata, generated capability inventories, prompt bodies, and descriptor details remain in structured resources, descriptors, tools, and prompts.

Status: addressed.

### Finding 19: Bootstrap resource tree creation was implicit

Risk: future implementation might forget that `mcp/bootstrap/src/main/resources` is new in this module and only verify source placement.

Reconciliation: the source map and plan now state that the accepted path creates the bootstrap resource tree and must be verified in the bootstrap jar and distribution copy.

Status: addressed.

### Finding 20: "Resource file" could be confused with MCP resources

Risk: reviewers might interpret `server-instructions.md` as a new protocol resource and expose it through `resources/list`, duplicating initialize instructions.

Reconciliation: the spec and plan now state that the Markdown file is an internal classpath resource, not a new MCP `resources/list` entry or `shardingsphere://` URI.

Status: addressed.

### Finding 21: Latest documentation could drift after implementation

Risk: citing `latest` directly could make future reviewers think the implementation must track a newer MCP revision without a deliberate protocol bump.

Reconciliation: the source map now records that `latest` redirects to `2025-11-25` as of 2026-05-17.
Implementation evidence should cite explicit `2025-11-25` URLs and use `latest` only as a drift check.

Status: addressed.

### Finding 22: Instruction hot reload would create session inconsistency

Risk: if Markdown is re-read dynamically, different clients or calls in the same server lifecycle could see different instruction text.

Reconciliation: the plan now forbids hot reload in this package.
Instruction changes take effect on server reconstruction or restart.

Status: addressed.

### Finding 23: Prompt loader changes could weaken prompt validation

Risk: shared Markdown loading affects prompt templates, and MCP prompt docs require input and output validation.

Reconciliation: the plan now requires prompt input/output validation expectations to remain unchanged after loader changes.

Status: addressed.

## Remaining Reanalysis Questions

- No remaining design question appears worth further analysis before implementation authorization.
- Cross-model review was not invoked because this round has no explicit external CLI authorization.
  Offer cross-model review again before code implementation if the implementation plan changes materially.
