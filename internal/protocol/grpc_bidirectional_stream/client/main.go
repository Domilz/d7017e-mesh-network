package main

import (
	"context"
	"flag"
	"io"
	"log"
	"time"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/internal/protocol/utils"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var (
	serverAddress = flag.String("serverAddress", "127.0.0.1:50051", "the address to the server for client connection")
)

func main() {
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

	request := prepareRequest()
	propagationStream, err := openBidirectionalStream(ctx, propagationClient)
	if err != nil {
		log.Fatalf("error opening stream: %v", err)
	}
	sendRequestToStream(propagationStream, request)

	// Receive and process the server response
	response, err := receiveAndProcessResponse(propagationStream)
	if err != nil {
		log.Fatalf("error receiving stream: %v", err)
	}

	log.Println("Fetching state stream from server...")
	utils.PrintFormattedState(response)

	// Close the stream
	if err := closeStream(propagationStream); err != nil {
		log.Println(err)
	}
}

func connectToServer() (*grpc.ClientConn, error) {
	conn, err := grpc.Dial(*serverAddress, grpc.WithTransportCredentials(insecure.NewCredentials()))
	return conn, err
}

func prepareRequest() *pb.State {
	// Mocked `Reading`
	reading := &pb.Reading{
		TagId:    "20",
		DeviceId: "21",
		Rssi:     69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
	}

	// Mocked `State`
	request := &pb.State{
		TagId: "20",
		Readings: []*pb.Reading{
			reading,
		},
	}
	return request
}

func openBidirectionalStream(ctx context.Context, client pb.StatePropagationClient) (pb.StatePropagation_PropagationClient, error) {
	propagationStream, err := client.Propagation(ctx)
	return propagationStream, err
}

func sendRequestToStream(stream pb.StatePropagation_PropagationClient, request *pb.State) {
	err := stream.Send(request)
	if err != nil {
		log.Fatalf("error when sending request: %v", err)
	}
}

func receiveAndProcessResponse(stream pb.StatePropagation_PropagationClient) (*pb.State, error) {
	response, err := stream.Recv()
	if err == io.EOF {
		return nil, err
	} else if err == nil {
		return response, nil

	}
	return nil, err
}

func closeStream(stream pb.StatePropagation_PropagationClient) error {
	err := stream.CloseSend()
	return err
}
