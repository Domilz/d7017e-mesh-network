package tag

import (
	"fmt"

	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/utils"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes/timestamp"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type Client struct {
	stateHandler *utils.StateHandler
}

func TagMain() {
	client := GetClient()
	client.SetupClient("daniel")
	client.InsertSingleMockedReading()
	state, _ := client.GetState()
	client.GetReadableOfSingleState(state)

}

func (client *Client) SetupClient(id string) {
	sh := &utils.StateHandler{}
	sh.InitStateHandler(id)
	client.stateHandler = sh
}

func (client *Client) GetReadableOfSingleState(state []byte) {
	var reading = &pb.State{}
	err := proto.Unmarshal(state, reading)
	if err != nil {
		fmt.Println(err)
		return
	}
	str := fmt.Sprintf("%v", reading)
	fmt.Println(str)
}

func (client *Client) GetState() ([]byte, error) {
	return client.stateHandler.GetSerializedState()
}

func (client *Client) Insert(serialized []byte) {
	client.stateHandler.InsertSerializedState(serialized)
}

func (client *Client) InsertSingleMockedReading() {
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
