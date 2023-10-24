package main

import (
	"flag"
	"fmt"
	"io"
	"log"
	"net"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/internal/protocol/utils"
	"google.golang.org/grpc"
)

var (
	serverAddress = flag.String("serverAddress", "127.0.0.1:50051", "the address to the server for client connection")
	port          = flag.Int("port", 50051, "The server port")
	stateHandler  utils.StateHandler
)

type server struct {
	pb.UnimplementedStatePropogationServer
}

func (s *server) Propogation(srv pb.StatePropogation_PropogationServer) error {
	log.Println("Fetching state stream from client...")

	ctx := srv.Context()

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()

		default:

		}

		// Receive and process requests from the client
		if err := processClientRequest(srv); err != nil {
			return err
		}
	}
}

func processClientRequest(srv pb.StatePropogation_PropogationServer) error {
	request, err := srv.Recv()
	if err == io.EOF {
		return err
	}
	if err != nil {
		log.Printf("receive error: %v", err)
		return err
	}

	// Handle the client request and send a response
	if err := handleClientRequest(request, srv); err != nil {
		return err
	}

	return nil
}

func handleClientRequest(request *pb.State, srv pb.StatePropogation_PropogationServer) error {
	// Print the `State` and `Reading` from the client
	fmt.Println("ACK")
	stateHandler.InsertMultipleReadings(request)
	state := stateHandler.GetState()
	utils.PrintFormattedState(state)

	return nil
}

func main() {
	stateHandler.InitStateHandler("MOCKED STATE")
	flag.Parse()

	listener, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterStatePropogationServer(s, &server{})

	log.Printf("server listening at: %s", *serverAddress)

	if err := s.Serve(listener); err != nil {
		log.Fatalf("failed to build server: %v", err)
	}
}
