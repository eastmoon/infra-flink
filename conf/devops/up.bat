@rem ------------------- batch setting -------------------
@echo off

@rem ------------------- declare variable -------------------

@rem ------------------- execute script -------------------
call :%*
goto end

@rem ------------------- declare function -------------------

:up-prepare (
    @rem Create .env for compose
    echo Current Environment %PROJECT_ENV%
    echo PROJECT_NAME=%PROJECT_NAME% > %CONF_FILE_PATH%

    @rem Setting cache directory
    set TARGET_DIR=%CLI_DIRECTORY%cache
    IF NOT EXIST %TARGET_DIR% (mkdir %TARGET_DIR%)
    echo CACHE_DIR=%TARGET_DIR% >> %CONF_FILE_PATH%

    goto end
)

:action
    echo ^> Server startup
    @rem Execute prepare action
    call :up-prepare

    @rem Run next deveopment with stdout
    docker-compose -f .\conf\docker\docker-compose.yml --env-file %CONF_FILE_PATH% up -d --build
    goto end

:args
    goto end

:short
    echo Startup Server
    goto end

:help
    echo This is a Command Line Interface with project %PROJECT_NAME%
    echo Startup Server
    echo.
    echo Options:
    echo      --help, -h        Show more information with '%~n0' command.
    call %CLI_SHELL_DIRECTORY%\utils\tools.bat command-description %~n0
    goto end

:end
