#!/usr/bin/env python3

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Test policy case contract validation."""

from __future__ import annotations

import copy
import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch

RUN_SPEC = importlib.util.spec_from_file_location("agents_harness_run", Path(__file__).with_name("run.py"))
if RUN_SPEC is None or RUN_SPEC.loader is None:
    raise RuntimeError("Unable to load agents harness run.py")
run = importlib.util.module_from_spec(RUN_SPEC)
RUN_SPEC.loader.exec_module(run)


class RunTest(unittest.TestCase):
    """Test the agents harness runner."""

    def setUp(self) -> None:
        self.case = {
            "id": "test_case",
            "group": "test",
            "description": "Valid test case.",
            "phase": "test",
            "ordinary_loop": False,
            "required_actions": [],
            "allowed_actions": [],
            "forbidden_actions": [],
            "required_reasons": [],
        }

    def test_duplicate_action(self) -> None:
        with patch.object(run, "ACTIONS", [*run.ACTIONS, run.ACTIONS[0]]), self.assertRaisesRegex(ValueError, "Duplicate actions"):
            run.validate_cases([self.case])

    def test_duplicate_reason(self) -> None:
        with patch.object(run, "REASONS", [*run.REASONS, run.REASONS[0]]), self.assertRaisesRegex(ValueError, "Duplicate reasons"):
            run.validate_cases([self.case])

    def test_unknown_action(self) -> None:
        for field in ("required_actions", "allowed_actions", "forbidden_actions"):
            with self.subTest(field=field):
                case = copy.deepcopy(self.case)
                case[field] = ["unknown_action"]
                with self.assertRaisesRegex(ValueError, "Unknown actions"):
                    run.validate_cases([case])

    def test_unknown_reason(self) -> None:
        self.case["required_reasons"] = ["unknown_reason"]
        with self.assertRaisesRegex(ValueError, "Unknown reasons"):
            run.validate_cases([self.case])

    def test_required_action_must_be_allowed(self) -> None:
        self.case["required_actions"] = ["inspect_local"]
        with self.assertRaisesRegex(ValueError, "Required actions must be allowed"):
            run.validate_cases([self.case])

    def test_allowed_and_forbidden_actions_must_not_overlap(self) -> None:
        self.case["allowed_actions"] = ["inspect_local"]
        self.case["forbidden_actions"] = ["inspect_local"]
        with self.assertRaisesRegex(ValueError, "Allowed and forbidden actions overlap"):
            run.validate_cases([self.case])


if __name__ == "__main__":
    unittest.main()
