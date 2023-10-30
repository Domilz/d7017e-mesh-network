package main

import (
	debuggLog "github.com/Domilz/d7017e-mesh-network/internal/backend/debuggLog"
)

func main() {

	debuggLog.StartDebuggLogServer("debuggLog/database/DebuggLogDatabase.db", "./debuggLog/debuggLogFormat.html")

	select {}
}
