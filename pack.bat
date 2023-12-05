@echo off

rmdir /s /q publish
mkdir publish
mkdir publish\misc\.idea
mkdir publish\misc\work

xcopy /s src publish
xcopy /s .idea publish\misc\.idea
xcopy /s work publish\misc\work
xcopy ToMiCompiler4J.iml publish\misc
xcopy pack.bat publish\misc
xcopy unpack.bat publish

cd publish
tar -a -c -f "..\ToMiC4J.zip" *
cd ..
