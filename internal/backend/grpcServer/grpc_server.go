package grpcserver

import (
	"flag"
	"fmt"
	"io"
	"log"
	"net"

	handler "github.com/Domilz/d7017e-mesh-network/internal/backend/handlers"
	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/internal/protocol/utils"
	"google.golang.org/grpc"
)

var (
	serverAddress       = flag.String("serverAddress", "127.0.0.1:50051", "the address to the server for client connection")
	port                = flag.Int("port", 50051, "The server port")
	backendStateHandler handler.BackendStateHandler
)

type server struct {
	pb.UnimplementedStatePropagationServer
}

func (s *server) Propagation(srv pb.StatePropagation_PropagationServer) error {
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

func processClientRequest(srv pb.StatePropagation_PropagationServer) error {
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

func handleClientRequest(request *pb.State, srv pb.StatePropagation_PropagationServer) error {

	fmt.Println("Recived state from client")
	utils.PrintFormattedState(request)
	backendStateHandler.InsertMultipleReadings(request)
	respState := &pb.State{TagId: "Received state"}
	if err := srv.Send(respState); err != nil {
		log.Printf("send error %v", err)
		return err
	}

	return nil
}

func StartGrpcServer() {
	backendStateHandler.InitStateHandler("SERVER-TAG")
	flag.Parse()

	listener, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterStatePropagationServer(s, &server{})

	log.Printf("server listening at: %s", *serverAddress)

	if err := s.Serve(listener); err != nil {
		log.Fatalf("failed to build server: %v", err)
	}
}

func SideStepGRPCServer(serializedState []byte) {
	state, err := utils.DeserializeState(serializedState)
	if err != nil {
		log.Println("Error during DeserializeState", err)
	} else {
		backendStateHandler.InsertMultipleReadings(state)
	}

}
