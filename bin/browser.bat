@rem master version of browser.bat is maintained in bin directory

@IF     EXIST "%1" GOTO ARGSFOUND

ant run
@GOTO DONE

:ARGSFOUND
@echo please use full path to scene
ant -Dargs=%1 run

:DONE

@rem debug:
@rem ECHO command-line arguments: %1
@rem PAUSE
