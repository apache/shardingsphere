#!/usr/bin/env python3
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

"""Exercise repository Checkstyle rules against synthetic Java sources."""

from __future__ import annotations

import subprocess
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[5]
BOOLEAN_MESSAGE = "Use assertTrue/assertFalse instead of assertThat(..., is(true/false))."
RULE_MESSAGES = {BOOLEAN_MESSAGE}


class CheckstyleRulesTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls) -> None:
        target_directory = REPOSITORY_ROOT / "target"
        target_directory.mkdir(exist_ok=True)
        temporary_directory = tempfile.TemporaryDirectory(prefix="checkstyle-rules-", dir=target_directory)
        cls.addClassCleanup(temporary_directory.cleanup)
        directory = Path(temporary_directory.name)
        cls.boolean_cases = {
            "literal_true": ("assertThat(actual, is(true));", True),
            "literal_false": ("assertThat(actual, is(false));", True),
            "boxed_true": ("assertThat(actual, is(Boolean.TRUE));", True),
            "boxed_false": ("assertThat(actual, is(Boolean.FALSE));", True),
            "qualified_true": ("assertThat(actual, is(java.lang.Boolean.TRUE));", True),
            "qualified_false": ("assertThat(actual, is(java.lang.Boolean.FALSE));", True),
            "qualified_matchers": ("org.hamcrest.MatcherAssert.assertThat(actual, org.hamcrest.Matchers.is(Boolean.TRUE));", True),
            "message": ('assertThat("foo", actual, is(Boolean.FALSE));', True),
            "variable": ("assertThat(actual, is(expected));", False),
            "string_literal": ('assertThat(actual, is("true"));', False),
            "other_constant": ("assertThat(actual, is(Foo.TRUE));", False),
            "other_boolean_type": ("assertThat(actual, is(foo.Boolean.TRUE));", False),
            "comment": ("// assertThat(actual, is(Boolean.TRUE));", False),
            "string_content": ('String example = "assertThat(actual, is(Boolean.TRUE));";', False),
        }
        cls.fixture_paths = {}
        for name, (statement, _) in cls.boolean_cases.items():
            cls.write_fixture(directory, f"boolean_{name}", f"void assertScenario() {{\n        {statement}\n    }}")
        properties = ET.parse(REPOSITORY_ROOT / "pom.xml").getroot().find("{*}properties")
        cls.engine_version = properties.findtext("{*}checkstyle.version")
        cls.write_pom(directory, properties.findtext("{*}maven-checkstyle-plugin.version"))
        completed = subprocess.run(
            [str(REPOSITORY_ROOT / "mvnw"), "-B", "-o", "-f", str(directory / "pom.xml"), "checkstyle:check"],
            cwd=REPOSITORY_ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=60,
        )
        if completed.returncode:
            raise AssertionError(f"Checkstyle fixture execution failed ({completed.returncode}):\n{completed.stdout[-6000:]}")
        report = ET.parse(directory / "checkstyle-result.xml").getroot()
        if cls.engine_version != report.get("version"):
            raise AssertionError(f"Expected Checkstyle {cls.engine_version}, got {report.get('version')}")
        cls.actual_diagnostics = {
            Path(file.get("name")).resolve(): [
                (error.get("message"), error.get("severity")) for error in file.findall("error")
                if error.get("message") in RULE_MESSAGES
            ]
            for file in report.findall("file")
        }
        if set(cls.fixture_paths.values()) != set(cls.actual_diagnostics):
            raise AssertionError("Checkstyle did not report exactly the selected fixture files")

    @classmethod
    def write_fixture(cls, directory: Path, name: str, method: str) -> None:
        path = directory / "sources" / name / "FooTest.java"
        path.parent.mkdir(parents=True)
        path.write_text(f"class FooTest {{\n    {method}\n}}\n", encoding="utf-8")
        cls.fixture_paths[name] = path.resolve()

    @classmethod
    def write_pom(cls, directory: Path, plugin_version: str) -> None:
        project = ET.Element("project", {"xmlns": "http://maven.apache.org/POM/4.0.0"})
        for name, value in (("modelVersion", "4.0.0"), ("groupId", "org.apache.shardingsphere"),
                            ("artifactId", "checkstyle-rule-fixtures"), ("version", "1"), ("packaging", "pom")):
            ET.SubElement(project, name).text = value
        properties = ET.SubElement(project, "properties")
        ET.SubElement(properties, "project.build.sourceEncoding").text = "UTF-8"
        build = ET.SubElement(project, "build")
        ET.SubElement(build, "sourceDirectory").text = str(directory / "sources")
        plugin = ET.SubElement(ET.SubElement(build, "plugins"), "plugin")
        for name, value in (("groupId", "org.apache.maven.plugins"), ("artifactId", "maven-checkstyle-plugin"), ("version", plugin_version)):
            ET.SubElement(plugin, name).text = value
        dependency = ET.SubElement(ET.SubElement(plugin, "dependencies"), "dependency")
        for name, value in (("groupId", "com.puppycrawl.tools"), ("artifactId", "checkstyle"), ("version", cls.engine_version)):
            ET.SubElement(dependency, name).text = value
        configuration = ET.SubElement(plugin, "configuration")
        for name, value in (
            ("configLocation", str(REPOSITORY_ROOT / "src" / "resources" / "checkstyle.xml")),
            ("outputFile", str(directory / "checkstyle-result.xml")), ("cacheFile", str(directory / "checkstyle-cache")),
            ("includeResources", "false"), ("includeTestResources", "false"), ("failOnViolation", "false"), ("failsOnError", "false"),
        ):
            ET.SubElement(configuration, name).text = value
        ET.ElementTree(project).write(directory / "pom.xml", encoding="utf-8", xml_declaration=True)

    def test_boolean_assertions_use_boolean_helpers(self) -> None:
        for name, (_, violates) in self.boolean_cases.items():
            with self.subTest(name=name):
                expected = [(BOOLEAN_MESSAGE, "error")] if violates else []
                self.assertEqual(expected, self.actual_diagnostics[self.fixture_paths[f"boolean_{name}"]])


if __name__ == "__main__":
    unittest.main()
