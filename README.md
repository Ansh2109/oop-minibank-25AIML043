# MiniBank

## Requirements
JDK 17+

## Compile
Run these commands from the project root in PowerShell:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

## Run
```powershell
java -cp out service.MiniBank
```