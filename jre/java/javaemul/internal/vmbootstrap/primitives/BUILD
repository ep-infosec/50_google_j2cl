# Description:
#   j2cl/vmbootstrap/primitives: Primitive helper classes.
#
# Depends on the JRE JS.

load(":generate_primitive.bzl", "generate_primitive_type")

package(
    default_visibility = ["//jre/java:__pkg__"],
    licenses = ["notice"],
)

# You can see the generated file with following commands:
# blaze build transpiler/vmbootstrap/primitives:all
# gnome-open blaze-genfiles/transpiler/vmbootstrap/primitives/

generate_primitive_type(
    name = "double",
    shortName = "D",
)

generate_primitive_type(
    name = "float",
    shortName = "F",
)

generate_primitive_type(
    name = "int",
    shortName = "I",
)

generate_primitive_type(
    name = "short",
    shortName = "S",
)

generate_primitive_type(
    name = "byte",
    shortName = "B",
)

generate_primitive_type(
    name = "char",
    shortName = "C",
)

generate_primitive_type(
    name = "void",
    shortName = "_",
)

generate_primitive_type(
    name = "long",
    initValue = "Long.fromInt(0)",
    jsTypeName = "Long",
    shortName = "J",
)

generate_primitive_type(
    name = "boolean",
    initValue = "false",
    jsTypeName = "boolean",
    shortName = "Z",
)

filegroup(
    name = "primitives",
    srcs = glob(["*.js"]) + [
        ":src_double",
        ":src_float",
        ":src_long",
        ":src_int",
        ":src_short",
        ":src_byte",
        ":src_char",
        ":src_boolean",
        ":src_void",
    ],
)
