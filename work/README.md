# Working Directory

> You need to open this directory with WSL to run the scripts.

## File Lists

- `in.c`: The source file to be compiled. (You should create it manually.)
- `libsysy.h`: The header file of the standard library.
- `libsysy.c`: The source file of the standard library.
- `build.sh`: The script to compile the source file to LLVM IR.
- `run.sh`: The script to run the generated LLVM IR with the LLVM interpreter.
- `in.txt`: The input file for the LLVM interpreter. (You should create it manually.)
- `README.md`: This file.

## Usage

### Standard LLVM IR

You can use the scripts to generate standard LLVM IR using clang.

To compile `in.c` to standard LLVM IR, run:

```bash
./build.sh    # Compile in.c to LLVM IR 
./build.sh -a # Compile in.c and libsysy.c to LLVM IR, and link them
```

It will generate `std.ll` in the current directory.

To run the generated LLVM IR with input from `in.txt`, run:

```bash
./run.sh
```

> You can use `./run.sh -a` to compile and run standard LLVM IR in one step.


### Your LLVM IR

Set current configuration to LLVM to generate `in.c` to your LLVM IR. It will output `llvm.ll` in the current directory.

To link your LLVM IR with the standard library, run:

```bash
./build.sh dev
```

It will replace your `llvm.ll` with the linked LLVM IR.

To run your LLVM IR with input form `in.txt`, run:

```bash
./run.sh dev
```

> You can use `./run.sh dev -a` to link and run your LLVM IR in one step. But only once.
