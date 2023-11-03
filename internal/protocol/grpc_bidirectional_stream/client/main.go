package main

import (
	"context"
	"flag"
	"fmt"
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
	xx := []byte{10, 3, 49, 49, 49, 18, 35, 10, 3, 49, 49, 49, 18, 10, 83, 111, 109, 101, 95, 82, 80, 95, 73, 68, 24, 69, 34, 12, 8, 228, 226, 146, 170, 6, 16, 176, 156, 205, 200, 1, 40, 1}
	PrintDeserializedState(xx)
	//printSerializedState()
}
func main2() {
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

	request, err := prepareRequest("1", "10")
	if err != nil {
		log.Fatalf("error with prepared request: %v", err)
	}

	//request2, err := prepareRequest("1", "10")
	if err != nil {
		log.Fatalf("error with prepared request: %v", err)
	}

	propagationStream, err := openBidirectionalStream(ctx, propagationClient)
	if err != nil {
		log.Fatalf("error opening stream: %v", err)
	}

	sendRequestToStream(propagationStream, request)
	//sendRequestToStream(propagationStream, request2)

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

func prepareRequest(stateID string, readingID string) ([]byte, error) {
	// Mocked `Reading`
	reading := &pb.Reading{
		TagId: readingID,
		RpId:  "Some_RP_ID",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
		IsDirect: 1,
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
func prepareOldRequest(stateID string, readingID string) ([]byte, error) {
	// Mocked `Reading`

	reading := &pb.Reading{
		TagId: readingID,
		RpId:  "RpId not set ",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds - 10000,
			Nanos:   timestamppb.Now().Nanos - 10000,
		},
		IsDirect: 1,
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

func openBidirectionalStream(ctx context.Context, client pb.StatePropagationClient) (pb.StatePropagation_PropagationClient, error) {
	propagationStream, err := client.Propagation(ctx)
	return propagationStream, err
}

func sendRequestToStream(stream pb.StatePropagation_PropagationClient, request []byte) {
	deserializedRequest, err := utils.DeserializeState(request)
	if err != nil {
		log.Fatalf("error when sending request: %v", err)
	}

	err = stream.Send(deserializedRequest)
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

func printSerializedState() {
	request, err := prepareRequest("111", "111")
	if err != nil {
		log.Fatalf("error with prepared request: %v", err)
	}
	fmt.Println(request)
}
func PrintDeserializedState(serializedState []byte) {
	deserializedState, err := utils.DeserializeState(serializedState)
	if err != nil {
		fmt.Println("Error deserializeing data")
	} else {
		utils.PrintFormattedState(deserializedState)
	}

}
