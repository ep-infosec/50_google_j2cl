package(licenses = ["notice"])

java_library(
    name = "transpiler_lib",
    srcs = [
        "J2clTranspiler.java",
        "J2clTranspilerOptions.java",
    ],
    deps = [
        "//third_party:auto_value",
        "//third_party:guava",
        "//third_party:jsr305_annotations",
        "//transpiler/java/com/google/j2cl/common",
        "//transpiler/java/com/google/j2cl/transpiler/ast",
        "//transpiler/java/com/google/j2cl/transpiler/backend",
        "//transpiler/java/com/google/j2cl/transpiler/frontend",
        "//transpiler/java/com/google/j2cl/transpiler/passes",
    ],
)

java_library(
    name = "commandlinerunner_lib",
    srcs = ["J2clCommandLineRunner.java"],
    visibility = ["//transpiler/javatests/com/google/j2cl:__subpackages__"],
    deps = [
        ":transpiler_lib",
        "//third_party:args4j",
        "//third_party:auto_value",
        "//third_party:guava",
        "//transpiler/java/com/google/j2cl/common",
        "//transpiler/java/com/google/j2cl/transpiler/backend",
        "//transpiler/java/com/google/j2cl/transpiler/frontend",
    ],
)

java_binary(
    name = "J2clCommandLineRunner",
    jvm_flags = [
        "-XX:+TieredCompilation",
        "-Dj2cl.deterministicTimestamps=true",
    ],
    main_class = "com.google.j2cl.transpiler.J2clCommandLineRunner",
    runtime_deps = [":commandlinerunner_lib"],
)

java_library(
    name = "bazelbuilder_lib",
    srcs = ["BazelJ2clBuilder.java"],
    deps = [
        ":transpiler_lib",
        "//third_party:args4j",
        "//third_party:auto_value",
        "//third_party:guava",
        "//transpiler/java/com/google/j2cl/common",
        "//transpiler/java/com/google/j2cl/common/bazel",
        "//transpiler/java/com/google/j2cl/transpiler/backend",
        "//transpiler/java/com/google/j2cl/transpiler/frontend",
    ],
)

java_binary(
    name = "BazelJ2clBuilder",
    jvm_flags = [
        "-XX:+TieredCompilation",
        "-Dj2cl.deterministicTimestamps=true",
        "-Xss2M",
    ],
    main_class = "com.google.j2cl.transpiler.BazelJ2clBuilder",
    visibility = ["//build_defs/internal_do_not_use:__pkg__"],
    runtime_deps = [":bazelbuilder_lib"],
)

# J2CL binary with Kotlin compiler dependency.
# TODO(b/217287994): Remove after a regular dependency is allowed.
java_binary(
    name = "BazelJ2clBuilderWithKolinSupport",
    jvm_flags = [
        "-XX:+TieredCompilation",
        "-Dj2cl.deterministicTimestamps=true",
        "-Xss2M",
    ],
    main_class = "com.google.j2cl.transpiler.BazelJ2clBuilder",
    visibility = ["//build_defs/internal_do_not_use:__pkg__"],
    runtime_deps = [
        ":bazelbuilder_lib",
        "//transpiler/java/com/google/j2cl/transpiler/frontend/kotlin",
    ],
)
