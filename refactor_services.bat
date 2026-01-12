@echo off
setlocal enabledelayedexpansion

set SERVICE_DIR=c:\Users\li\IdeaProjects\fruit-shop\src\main\java\com\lingnan\fruitshop\service
set IMPL_DIR=%SERVICE_DIR%\impl

echo Starting service refactoring...

for %%f in ("%SERVICE_DIR%\*.java") do (
    set filename=%%~nf
    if not "!filename!"=="SmsCodeService" (
        if not exist "%SERVICE_DIR%\!filename!.java.bak" (
            echo Processing !filename!...
            
            REM Backup original file
            move "%SERVICE_DIR%\!filename!.java" "%SERVICE_DIR%\!filename!.java.bak"
            
            REM Create interface (simplified - just method signatures)
            echo Creating interface for !filename!...
            (
            echo package com.lingnan.fruitshop.service;
            echo.
            echo public interface !filename! {
            echo // TODO: Add method signatures here
            echo }
            ) > "%SERVICE_DIR%\!filename!.java"
            
            REM Create implementation (copy original and modify)
            echo Creating implementation for !filename!...
            (
            echo package com.lingnan.fruitshop.service.impl;
            echo.
            echo import com.lingnan.fruitshop.service.!filename!;
            echo import org.springframework.stereotype.Service;
            echo.
            echo @Service
            echo public class !filename!Impl implements !filename! {
            echo // TODO: Copy implementation from backup and add @Override annotations
            echo }
            ) > "%IMPL_DIR%\!filename!Impl.java"
            
            echo Created !filename!.java and !filename!Impl.java
        )
    )
)

echo Service refactoring completed!
echo Please manually add method signatures to interfaces and copy implementations with @Override annotations.
pause
