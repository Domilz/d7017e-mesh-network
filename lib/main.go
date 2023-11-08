package main

import (
	"log"
	"os"

	"github.com/Domilz/d7017e-mesh-network/pkg/backend"
	grpc "github.com/Domilz/d7017e-mesh-network/pkg/protocol/grpc/client"
)

func main() {
	// Main function, add more cases for main functions of packages when defined.
	args := os.Args[1:]
	switch args[0] {
	case "backend":
		log.Println("Starting Backend")
		backend.Main()
	case "grpc":
		log.Println("Starting GRPC Client")
		grpc.Main()
	default:
		log.Println("No Main function for given arg")
		return
	}
}
