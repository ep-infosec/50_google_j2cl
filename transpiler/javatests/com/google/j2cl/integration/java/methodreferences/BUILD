load(
    "//transpiler/javatests/com/google/j2cl/integration:integration_test.bzl",
    "integration_test",
)

package(licenses = ["notice"])

integration_test(
    name = "methodreferences",
    srcs = glob(["*.java"]),
    enable_kt_native = False,
    enable_rta = False,  # TODO(b/146458562): Enable RTA when this bug is fixed.
)
