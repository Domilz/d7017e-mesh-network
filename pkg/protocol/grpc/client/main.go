package grpc

import (
	"context"
	"flag"
	"fmt"
	"io"
	"log"
	"time"

	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/utils"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var (
	serverAddress2 = flag.String("serverAddress2", "83.233.46.128:50051", "the address to the server for client connection")
)

func Main() {
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
	conn, err := grpc.Dial(*serverAddress2, grpc.WithTransportCredentials(insecure.NewCredentials()))
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
	serializedState, err := state.GetSerializedState()
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
	serializedState, err := state.GetSerializedState()
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
