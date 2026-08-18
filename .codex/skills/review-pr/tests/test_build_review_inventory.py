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

import sys
import unittest
from argparse import Namespace
from pathlib import Path
from unittest.mock import patch


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import build_review_inventory as inventory


class BuildReviewInventoryTest(unittest.TestCase):

    @patch.object(inventory, "run_git")
    @patch.object(inventory, "get_repo_root", return_value=Path("/repo"))
    def test_build_inventory_uses_triple_dot_scope(self, _, run_git):
        run_git.side_effect = [
            "base-sha\n",
            "head-sha\n",
            "merge-base\n",
            "M\tsrc/main/java/Foo.java\nA\tdocs/guide.md\n",
            " M unrelated.java\n",
        ]
        args = Namespace(base_ref="base", head_ref="head", previous_head=None, github_files=None)
        actual = inventory.build_inventory(args)
        self.assertEqual("merge-base", actual["scope"]["merge_base"])
        self.assertEqual(2, actual["scope"]["changed_file_count"])
        self.assertEqual(1, actual["dirty_worktree"]["count"])
        self.assertEqual("src/main/java/Foo.java", actual["changed_files_by_category"]["production-java"][0]["path"])
        run_git.assert_any_call(["diff", "--name-status", "merge-base..head"], Path("/repo"))


if __name__ == "__main__":
    unittest.main()
