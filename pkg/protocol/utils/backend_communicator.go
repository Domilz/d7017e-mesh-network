package utils

import (
	"context"
	"flag"
	"fmt"
	"io"
	"log"
	"time"

	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

var (
	serverAddress = flag.String("serverAddress", "83.233.46.128:50051", "the address to the server for client connection")
)

func SendToBackend(state *pb.State) {
	flag.Parse()

	// Establish a connection to the gRPC server
	conn, err := connectToServer()
	if err != nil {
		log.Fatalf("error connecting to the gRPC server: %v", err)
	}
	defer conn.Close()

	// Create a gRPC client
	propagationClient := pb.NewStatePropagationClient(conn)

	// Prepare and send the client request
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	propagationStream, err := openBidirectionalStream(ctx, propagationClient)
	if err != nil {
		log.Fatalf("error opening stream: %v", err)
	}

	sendRequestToStream(propagationStream, state)

	// Receive and process the server response
	go receiveAndProcessResponse(propagationStream)
	if err != nil {
		log.Fatalf("error receiving stream: %v", err)
	}

	log.Println("Fetching state stream from server...")

	// Close the stream
	if err := closeStream(propagationStream); err != nil {
		log.Println(err)
	}

	select {}

}

func connectToServer() (*grpc.ClientConn, error) {
	conn, err := grpc.Dial(*serverAddress, grpc.WithTransportCredentials(insecure.NewCredentials()))
	return conn, err
}

func openBidirectionalStream(ctx context.Context, client pb.StatePropagationClient) (pb.StatePropagation_PropagationClient, error) {
	propagationStream, err := client.Propagation(ctx)
	return propagationStream, err
}

func sendRequestToStream(stream pb.StatePropagation_PropagationClient, state *pb.State) {

	err := stream.Send(state)
	if err != nil {
		log.Fatalf("error when sending request: %v", err)
	}
}

func receiveAndProcessResponse(stream pb.StatePropagation_PropagationClient) (*pb.State, error) {
	response, err := stream.Recv()
	if err == io.EOF {
		return nil, err
	} else if err == nil {
		if response != nil {
			//utils.PrintFormattedState(response)
			fmt.Println("server response: ", response.TagId)

		}

	}
	return nil, err
}

func closeStream(stream pb.StatePropagation_PropagationClient) error {
	err := stream.CloseSend()
	return err
}
