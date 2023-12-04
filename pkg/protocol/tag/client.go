package tag

import (
	"fmt"

	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/utils"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type Client struct {
	stateHandler   *utils.StateHandler
	backendAddress string
}

func Main() {
	client := GetClient()
	client.SetupClient("tagId5", "83.233.46.128:50051")
	client.InsertSingleMockedReading("tagId17")
	client.SendStateToServer()

}

func (client *Client) SetupClient(id string, address string) {
	sh := &utils.StateHandler{}
	sh.InitStateHandler(id)
	client.stateHandler = sh
	client.backendAddress = address
}

func (client *Client) GetDeserializedState() string {
	state := client.stateHandler.GetState()
	return fmt.Sprintf("%v", state)
}

func (client *Client) GetReadableOfProvidedSerializedState(state []byte) (string, error) {
	deserializedState, err := utils.DeserializeState(state)
	if err != nil {
		return "", err
	}
	str := fmt.Sprintf("%v", deserializedState)
	return str, nil
}

func (client *Client) GetSerializedState() ([]byte, error) {
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
		RpId:  "rpId19",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
			Nanos:   timestamppb.Now().Nanos,
		},
		IsDirect: 0,
	}
	client.stateHandler.InsertSingleReading(reading)
}

func (client *Client) SendStateToServer() {
	s := client.stateHandler.GetState()

	utils.SendToBackend(s, client.backendAddress)

}

// When a tag sees a beacon
func (client *Client) UpdateReadingofSelf(rpId string, rssi int32) {
	client.stateHandler.UpdateReadingofSelf(rpId, rssi)
}

func GetClient() *Client {
	return &Client{}
}
