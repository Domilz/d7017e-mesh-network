package tagClient

import (
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/utils"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type Client struct {
	stateHandler *utils.StateHandler
}

func (client *Client) SetupClient(id string) {
	client.stateHandler.InitStateHandler(id)
}

func (client *Client) GetState() ([]byte, error) {
	return client.stateHandler.GetSerializedState()
}

func (client *Client) Insert(serialized []byte) {
	client.stateHandler.InsertSerializedState(serialized)
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
