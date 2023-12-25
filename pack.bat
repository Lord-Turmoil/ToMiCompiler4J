@echo off

rmdir /s /q publish
mkdir publish
mkdir publish\misc\.idea
mkdir publish\misc\work

xcopy /q /s src publish
xcopy /q /s .idea publish\misc\.idea
xcopy /q /s work publish\misc\work
xcopy /q ToMiCompiler4J.iml publish\misc
xcopy /q pack.bat publish\misc
xcopy /q unpack.bat publish

del publish\misc\.idea\vcs.xml

cd publish
tar -a -c -f "..\ToMiC4J.zip" *
cd ..
