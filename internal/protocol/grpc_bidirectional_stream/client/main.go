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
	propogationClient := pb.NewStatePropogationClient(conn)

	// Prepare and send the client request
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	request, err := prepareRequest("1", "10")
	if err != nil {
		log.Fatalf("error with prepared request: %v", err)
	}

	request2, err := prepareRequest("1", "10")
	if err != nil {
		log.Fatalf("error with prepared request: %v", err)
	}

	propogationStream, err := openBidirectionalStream(ctx, propogationClient)
	if err != nil {
		log.Fatalf("error opening stream: %v", err)
	}

	sendRequestToStream(propogationStream, request)
	sendRequestToStream(propogationStream, request2)

	// Receive and process the server response
	go receiveAndProcessResponse(propogationStream)
	// response, err := receiveAndProcessResponse(propogationStream)
	if err != nil {
		log.Fatalf("error receiving stream: %v", err)
	}

	log.Println("Fetching state stream from server...")
	// utils.PrintFormattedState(response)

	// Close the stream
	if err := closeStream(propogationStream); err != nil {
		log.Println(err)
	}

	select {}

}

func connectToServer() (*grpc.ClientConn, error) {
	conn, err := grpc.Dial(*serverAddress, grpc.WithTransportCredentials(insecure.NewCredentials()))
	return conn, err
}

func prepareRequest(stateID string, readingID string) ([]byte, error) {
	// Mocked `Reading`
	reading := &pb.Reading{
		TagId: readingID,
		RpId:  "RpId not set ",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
	}

	// Mocked `State`
	request := &pb.State{
		TagId: stateID,
		Readings: []*pb.Reading{
			reading,
		},
	}

	// Get the state
	state := utils.StateHandler{}
	state.InitStateHandler(stateID)
	state.InsertMultipleReadings(request)
	serializedState, err := state.GetState()
	if err != nil {
		return nil, err
	}
	return serializedState, nil
}

func openBidirectionalStream(ctx context.Context, client pb.StatePropogationClient) (pb.StatePropogation_PropogationClient, error) {
	propogationStream, err := client.Propogation(ctx)
	return propogationStream, err
}

func sendRequestToStream(stream pb.StatePropogation_PropogationClient, request []byte) {
	deserializedRequest, err := utils.DeserializeState(request)
	if err != nil {
		log.Fatalf("error when sending request: %v", err)
	}

	err = stream.Send(deserializedRequest)
	if err != nil {
		log.Fatalf("error when sending request: %v", err)
	}
}

func receiveAndProcessResponse(stream pb.StatePropogation_PropogationClient) (*pb.State, error) {
	response, err := stream.Recv()
	if err == io.EOF {
		return nil, err
	} else if err == nil {
		if response != nil {
			utils.PrintFormattedState(response)

		}

	}
	return nil, err
}

func closeStream(stream pb.StatePropogation_PropogationClient) error {
	err := stream.CloseSend()
	return err
}
