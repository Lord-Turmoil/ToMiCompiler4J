#!/bin/bash

if [ "$1" = "dev" ]; then
    filename=${2:-"llvm.ll"}
    echo "===== DEV:"
    echo "Compiling libsysy.c to lib.ll..."
    clang -S -emit-llvm libsysy.c -o lib.ll -O0

    echo "Linking $filename and lib.ll to std.ll..."
    llvm-link $filename lib.ll -S -o out.ll

    if [ $? -ne 0 ]; then
        echo "Linking failed!"./
        exit 1
    fi

    rm -rf llvm.ll
    mv out.ll llvm.ll
elif [ "$1" = "clean" ]; then
    echo "Cleaning workspace..."
    rm -rf llvm.ll std.ll
    rm -rf llvm_ir.txt
else
    echo "===== STD:"
    echo "Compiling libsysy.c to lib.ll..."
    clang -S -emit-llvm libsysy.c -o lib.ll -O0

    echo "Compiling      in.c to out.ll..."
    clang -S -emit-llvm in.c -o out.ll -O0

    if [ "$1" = "-a" ]; then
        echo "Linking out.ll and lib.ll to std.ll..."
        llvm-link out.ll lib.ll -S -o std.ll
    else
        mv out.ll std.ll
    fi
fi

echo "Clearing up..."
rm -rf out.ll lib.ll
