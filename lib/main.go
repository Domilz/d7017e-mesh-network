package main

import (
	"fmt"
	"log"
	"os"
	"strings"

	"github.com/Domilz/d7017e-mesh-network/pkg/backend"
	grpc "github.com/Domilz/d7017e-mesh-network/pkg/protocol/grpc/client"
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/tag"
)

func main() {
	// Main function, add more cases for main functions of packages when defined.
	fmt.Println(os.Args)
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
	case "grpc":
		log.Println("Starting GRPC Client")
		grpc.Main()
	case "tag":
		log.Println("Starting Tag Client")
		tag.Main()
	default:
		log.Println("No Main function for given arg")
		return
	}
}
