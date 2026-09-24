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
import xml.etree.ElementTree as ET
from pathlib import Path


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import collect_quality_baseline as baseline


class CollectQualityBaselineTest(unittest.TestCase):

    def test_missing_target_class_is_not_covered(self):
        root = ET.fromstring("<report />")
        found, counters, missed_branch_lines = baseline.summarize_target_coverage(root, "sample.MissingClass")
        self.assertFalse(found)
        self.assertEqual({"CLASS": None, "LINE": None, "BRANCH": None}, counters)
        self.assertEqual([], missed_branch_lines)

    def test_aggregate_target_and_inner_class_coverage(self):
        root = ET.fromstring("""
            <report>
              <package name="sample">
                <class name="sample/Target" sourcefilename="Target.java">
                  <counter type="CLASS" missed="0" covered="1" />
                  <counter type="LINE" missed="1" covered="4" />
                  <counter type="BRANCH" missed="1" covered="3" />
                </class>
                <class name="sample/Target$Inner" sourcefilename="Target.java">
                  <counter type="CLASS" missed="0" covered="1" />
                  <counter type="LINE" missed="0" covered="2" />
                  <counter type="BRANCH" missed="0" covered="2" />
                </class>
                <sourcefile name="Target.java">
                  <line nr="12" mb="1" cb="1" />
                </sourcefile>
              </package>
            </report>
        """)
        found, counters, missed_branch_lines = baseline.summarize_target_coverage(root, "sample.Target")
        self.assertTrue(found)
        self.assertEqual((2, 0, 100.0), counters["CLASS"])
        self.assertEqual((6, 1, 6 * 100.0 / 7), counters["LINE"])
        self.assertEqual((5, 1, 5 * 100.0 / 6), counters["BRANCH"])
        self.assertEqual([12], missed_branch_lines)

    def test_branchless_target_satisfies_target(self):
        root = ET.fromstring("""
            <report>
              <package name="sample">
                <class name="sample/Target" sourcefilename="Target.java">
                  <counter type="CLASS" missed="0" covered="1" />
                  <counter type="LINE" missed="0" covered="1" />
                </class>
                <sourcefile name="Target.java">
                  <line nr="1" mb="0" cb="0" />
                </sourcefile>
              </package>
            </report>
        """)
        found, counters, missed_branch_lines = baseline.summarize_target_coverage(root, "sample.Target")
        self.assertTrue(found)
        self.assertEqual((0, 0, 100.0), counters["BRANCH"])
        self.assertEqual([], missed_branch_lines)
        self.assertTrue(baseline.meets_target(found, counters, 100.0))

    def test_missing_line_counter_does_not_satisfy_target(self):
        root = ET.fromstring("""
            <report>
              <package name="sample">
                <class name="sample/Target" sourcefilename="Target.java">
                  <counter type="CLASS" missed="0" covered="1" />
                </class>
              </package>
            </report>
        """)
        found, counters, _ = baseline.summarize_target_coverage(root, "sample.Target")
        self.assertTrue(found)
        self.assertIsNone(counters["LINE"])
        self.assertEqual((0, 0, 100.0), counters["BRANCH"])
        self.assertFalse(baseline.meets_target(found, counters, 100.0))


if __name__ == "__main__":
    unittest.main()
