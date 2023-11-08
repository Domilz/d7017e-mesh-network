package tag

import (
	"fmt"

	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/utils"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type Client struct {
	stateHandler *utils.StateHandler
}

func Main() {
}

func (client *Client) SetupClient(id string) {
	sh := &utils.StateHandler{}
	sh.InitStateHandler(id)
	client.stateHandler = sh
}

func (client *Client) GetReadableOfSingleState(state []byte) (string, error) {
	deserializedState, err := utils.DeserializeState(state)
	if err != nil {
		return "", err
	}
	str := fmt.Sprintf("%v", deserializedState)
	return str, nil
}

func (client *Client) GetState() ([]byte, error) {
	return client.stateHandler.GetSerializedState()
}

func (client *Client) Insert(serialized []byte) error {
	err := client.stateHandler.InsertSerializedState(serialized)
	if err != nil {
		return err
	}
	return nil
}

func (client *Client) InsertSingleMockedReading(tagId string) {
	reading := &pb.Reading{
		TagId: tagId,
		RpId:  "TestRpId",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
		IsDirect: 1,
	}
	client.stateHandler.InsertSingleReading(reading)
}

func (client *Client) SendToServer() {
	reading := &pb.Reading{
		TagId: "TestTadId",
		RpId:  "TestRpId",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
		IsDirect: 1,
	}
	client.stateHandler.InsertSingleReading(reading)
	s := client.stateHandler.GetState()

	utils.SendToBackend(s)

}

func (client *Client) UpdateReadingofSelf(rpId string) {

}

func GetClient() *Client {
	return &Client{}
}
