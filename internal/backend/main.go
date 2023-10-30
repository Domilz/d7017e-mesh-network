package main

import (
	debugLog "github.com/Domilz/d7017e-mesh-network/internal/backend/debugLog"
)

func main() {

	debugLog.StartDebugLogServer("debugLog/database/DebugLogDatabase.db", "./debugLog/debugLogFormat.html")

	select {}
}
