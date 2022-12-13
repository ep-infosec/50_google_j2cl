# Copyright 2017 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Util funcs for running Blaze on int. tests in live  and j2size repo."""

import bz2
import getpass
import multiprocessing
import os
import re
import signal
import subprocess

INTEGRATION_ROOT = "transpiler/javatests/com/google/j2cl/integration/"
OBFUSCATED_OPT_TEST_PATTERN = INTEGRATION_ROOT + "%s:optimized_js%s"
READABLE_OPT_TEST_PATTERN = INTEGRATION_ROOT + "%s:readable_optimized_js%s"
SIZE_REPORT = INTEGRATION_ROOT + "size_report.txt"
TEST_LIST = INTEGRATION_ROOT + "optimized_js_list.bzl"
BENCH_ROOT = "benchmarking/java/com/google/j2cl/benchmarks/"
JVM_BENCH_PATTERN = BENCH_ROOT + "%s"
J2CL_BENCH_PATTERN = BENCH_ROOT + "%s-j2cl"
J2WASM_BENCH_PATTERN = BENCH_ROOT + "%s-j2wasm"


def get_benchmarks(bench_name, platforms):
  """Returns the targets for given benchmark name."""
  benchmarks = {}
  if "JVM" in platforms:
    benchmarks["JVM"] = JVM_BENCH_PATTERN % bench_name
  if "CLOSURE" in platforms:
    benchmarks["J2CL"] = J2CL_BENCH_PATTERN % bench_name
  if "WASM" in platforms:
    benchmarks["J2WASM"] = J2WASM_BENCH_PATTERN % bench_name
  return benchmarks


def build_original_and_modified(original_targets, modified_targets):
  """Blaze builds provided original/modified integration tests in parallel."""

  # Create a pool that does not capture ctrl-c.
  original_sigint_handler = signal.signal(signal.SIGINT, signal.SIG_IGN)
  pool = multiprocessing.Pool(processes=2)
  signal.signal(signal.SIGINT, original_sigint_handler)

  original_result = pool.apply_async(
      build_tests, [original_targets, get_j2size_repo_path()],
      callback=lambda x: print("    Original done building."))

  modified_result = pool.apply_async(
      build_tests, [modified_targets],
      callback=lambda x: print("    Modified done building."))
  pool.close()
  pool.join()

  # Invoke get() on async results to "propagate" the exceptions that
  # were raised if any.
  original_result.get()
  modified_result.get()


def build_tests(test_targets, cwd=None):
  """Blaze builds provided integration tests in parallel."""
  js_targets = [t + ".js" for t in test_targets]
  run_cmd(["blaze", "build"] + js_targets, cwd=cwd)


def get_optimized_test(test_name):
  """Returns the path to the obfuscated opt JS file the given test."""
  return OBFUSCATED_OPT_TEST_PATTERN % parse_name(test_name)


def get_readable_optimized_test(test_name):
  """Returns the path to the readable opt JS file the given test."""
  return READABLE_OPT_TEST_PATTERN % parse_name(test_name)


def get_all_tests(test_name):
  """Returns the path to the obfuscated opt JS file the given test."""
  return INTEGRATION_ROOT + test_name + ":all"


def create_test_filter(platforms):
  """Returns filter based on platform or empty if all platforms are enabled."""

  tags = []
  if "CLOSURE" not in platforms:
    tags += ["-j2cl"]
  if "WASM" not in platforms:
    tags += ["-j2wasm"]
  if "JVM" not in platforms:
    tags += ["-jvm"]
  if "J2KT" not in platforms:
    tags += ["-j2kt"]

  return ["--build_tests_only"
         ] + (["--test_tag_filters=" + ",".join(tags)] if tags else [])


def parse_name(test_name):
  """Parses a test name into a tuple contains the test name and the version."""
  (name, sep, version) = test_name.partition(".")
  return (name, sep + version)


def get_all_size_tests(cwd=None):
  """Returns all size tests."""

  bundled = [t + "-bundle" for t in _get_tests_with_tag("j2size_bundle", cwd)]
  optimized = _get_tests_with_tag("j2size_opt", cwd)
  return (bundled, optimized)


def _get_tests_with_tag(tag, cwd=None):
  command = [
      "blaze", "query",
      "attr(\"tags\",\"%s\",%s...)" % (tag, INTEGRATION_ROOT)
  ]

  return run_cmd(command, cwd=cwd).splitlines()


def get_js_files_by_test_name(test_targets):
  """Finds and returns a test_name<->optimized_js_file map."""

  # Convert to a map of names<->jsFile pairs
  test_names = [_get_test_name(size_target) for size_target in test_targets]
  js_files = [get_file_from_target(t) for t in test_targets]
  return dict(zip(test_names, js_files))


def _get_test_name(target):
  """Returns the test name for a target."""

  pattern = re.compile(INTEGRATION_ROOT + r"((?:java|kotlin))/(\w+):[\w-]+((.\w+)?)")
  search_results = pattern.search(target)
  return search_results.group(2) + "/" + search_results.group(1) + search_results.group(3)


CLOSURE_LICENSE = """/*

 Copyright The Closure Library Authors.
 SPDX-License-Identifier: Apache-2.0
*/
"""


def get_compressed_size(file_name):
  if not os.path.exists(file_name):
    return -1
  with open(file_name, "r") as f:
    # Drop closure license to reduce noise in size tests.
    contents = f.read().replace(CLOSURE_LICENSE, "")
    compressed_content = bz2.compress(contents.encode("utf-8"))
  return len(compressed_content)


def get_file_from_target(target, extension=".js"):
  return target.replace(":", "/") + extension


def sync_j2size_repo():
  g4_sync_cmds = [
      "synced_to_cl=@$(srcfs get_readonly) && " +
      "cd $(p4 g4d -f j2cl-size) && g4 sync $synced_to_cl"
  ]
  run_cmd(g4_sync_cmds, shell=True)


def get_j2size_repo_path():
  return "/google/src/cloud/%s/j2cl-size/google3" % getpass.getuser()


def run_cmd(cmd_args, cwd=None, include_stderr=False, shell=False):
  """Runs a command and returns output as a string."""

  process = subprocess.Popen(
      cmd_args,
      stdin=subprocess.PIPE,
      stdout=subprocess.PIPE,
      stderr=subprocess.PIPE,
      shell=shell,
      cwd=cwd)
  output = process.communicate()
  if process.wait() != 0:
    print("Error while running the command!")
    print("\nOUTPUT:\n============")
    print(output[1].decode("utf-8"))
    print("============\n")
    raise Exception("cmd invocation FAILED: " + " ".join(cmd_args))

  rv = output[0].decode("utf-8")
  if include_stderr:
    rv = (rv + "\n" if rv else "") + output[1].decode("utf-8")
  return rv
