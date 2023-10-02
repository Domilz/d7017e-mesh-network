package main

import (
	"flag"
	"fmt"
	"io"
	"log"
	"net"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/internal/protocol/utils"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var (
	serverAddress = flag.String("serverAddress", "localhost:50051", "the address to the server for client connection")
	port          = flag.Int("port", 50051, "The server port")
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
	utils.PrintFormattedState(request)

	// Mocked `Reading`
	reading := &pb.Reading{
		TagId:    "10",
		DeviceId: "11",
		Rssi:     20,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
	}

	// Mocked `State`
	resp := &pb.State{
		TagId: "10",
		Readings: []*pb.Reading{
			reading,
		},
	}

	// Send the `State` and `Reading` to the client
	if err := srv.Send(resp); err != nil {
		log.Printf("send error %v", err)
		return err
	}

	return nil
}

func main() {
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
