load(
    "//transpiler/javatests/com/google/j2cl/integration:integration_test.bzl",
    "integration_test",
)

package(licenses = ["notice"])

integration_test(
    name = "trywithresource",
    srcs = glob(["*.java"]),
    # TODO(b/170691676): Enable when full try/catch/finally semantics are
    # implemented.
    enable_wasm = False,
)
