package main

import (
	"log"
	"os"
	"strings"

	"github.com/Domilz/d7017e-mesh-network/pkg/backend"
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/tag"
)

func main() {
	// Main function, add more cases for main functions of packages when defined.
	args := os.Args[1:]
	if len(args) == 0 {
		log.Println("No arguments provided")
		return
	}

	mainArg := strings.ToLower(args[0])
	switch mainArg {
	case "backend":
		log.Println("Starting Backend")
		backend.Main()
	case "tag":
		log.Println("Starting Tag Client")
		tag.Main()
	case "test":
		log.Println("Testing StateDB")
		backend.TestStateDb()
	default:
		log.Println("No Main function for given arg")
		return
	}
}
