@rem ------------------- batch setting -------------------
@echo off

@rem ------------------- declare variable -------------------
if not defined TARGET_SERVER (set TARGET_SERVER=kafka)

@rem ------------------- execute script -------------------
call :%*
goto end

@rem ------------------- declare function -------------------

:action
    IF EXIST %CONF_FILE_PATH% (
        docker exec -ti docker-%TARGET_SERVER%_%PROJECT_NAME% bash
    )
    goto end

:args
    set KEY=%1
    set VALUE=%2
    if "%KEY%"=="--tag" (set TARGET_SERVER=%VALUE%)
    goto end

:short
    echo Into server
    goto end

:help
    echo This is a Command Line Interface with project %PROJECT_NAME%
    echo Into target server
    echo.
    echo Options:
    echo      --help, -h        Show more information with '%~n0' command.
    echo      --tag             Target service name, default is 'docker-%TARGET_SERVER%_%PROJECT_NAME%'
    call %CLI_SHELL_DIRECTORY%\utils\tools.bat command-description %~n0
    goto end

:end
