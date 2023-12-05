@echo off

mkdir src
move lib src
move tomic src
move ArgumentParser.java src
move Compiler.java src
move Main.java src
move config.json src

move misc\work .
move misc\.idea .
move misc\pack.bat .
move misc\ToMiCompiler4J.iml .

rmdir /s /q misc
