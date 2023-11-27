if [ "$1" = "dev" ]; then
    if [ "$2" = "-a" ]; then
       ./build.sh dev
    fi
    cat in.txt | lli llvm.ll
else
    if [ "$1" = "-a" ]; then
       build.sh -a
    fi
    cat in.txt | lli std.ll
fi