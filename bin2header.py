#!/usr/bin/env python3
"""
bin2header.py
Convert a binary file to a C header defining a byte array and size.

Usage:
    python3 bin2header.py input.bin output.h [--name NAME] [--guard GUARD]
                                             [--array-type "const unsigned char"]
                                             [--bytes-per-line N] [--align N]
"""

import argparse
import os


def format_bytes(data, per_line):
    lines = []
    for i in range(0, len(data), per_line):
        chunk = data[i : i + per_line]
        lines.append(", ".join(f"0x{b:02X}" for b in chunk))
    return lines


def main():
    p = argparse.ArgumentParser(description="Convert binary to C header")
    p.add_argument("infile", help="Input binary file")
    p.add_argument("outfile", help="Output header file")
    p.add_argument(
        "--name",
        default=None,
        help="Base name for symbols (default: derived from outfile)",
    )
    p.add_argument(
        "--guard", default=None, help="Include guard (default: derived from outfile)"
    )
    p.add_argument("--array-type", default="const unsigned char", help="Type for array")
    p.add_argument(
        "--bytes-per-line", type=int, default=12, help="Bytes per line in initializer"
    )
    p.add_argument(
        "--align",
        type=int,
        default=0,
        help="Add alignment attribute (byte count), 0 = none",
    )
    args = p.parse_args()

    with open(args.infile, "rb") as f:
        data = f.read()

    base = args.name or os.path.splitext(os.path.basename(args.outfile))[0]
    # sanitize base for identifier
    ident = "".join(c if (c.isalnum() or c == "_") else "_" for c in base)
    array_name = f"{ident}_data"
    size_name = f"{ident}_size"

    guard = (
        args.guard
        or f"_{os.path.splitext(os.path.basename(args.outfile))[0].upper()}_H_"
    )
    guard = "".join(c if (c.isalnum() or c == "_") else "_" for c in guard)

    lines = format_bytes(data, args.bytes_per_line)

    align_attr = ""
    if args.align and args.align > 0:
        # GCC/Clang style
        align_attr = f" __attribute__((aligned({args.align})))"
    header = []
    header.append(f"#ifndef {guard}")
    header.append(f"#define {guard}")
    header.append("")
    header.append("#include <stddef.h>")
    header.append("")
    header.append(
        f"/* Binary data from: {os.path.basename(args.infile)} (size: {len(data)} bytes) */"
    )
    header.append("")
    header.append(f"static {args.array_type}{align_attr} {array_name}[] = {{")
    for L in lines:
        header.append("    " + L + ",")
    if not lines:
        header.append("    /* empty */")
    header.append("};")
    header.append("")
    # header.append(f"static const size_t {size_name} = {len(data)};")
    # header.append("")
    header.append(f"#endif /* {guard} */")
    header_text = "\n".join(header) + "\n"

    with open(args.outfile, "w", newline="\n") as f:
        f.write(header_text)


if __name__ == "__main__":
    main()
