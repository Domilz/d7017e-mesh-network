package tag

import (
	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/utils"
)

type Client struct {
	stateHandler utils.StateHandler
}

func (client *Client) SetupClient(id string) {
	client.stateHandler.InitStateHandler(id)
}

func (client *Client) GetState() ([]byte, error) {
	return client.stateHandler.GetState()
}

func (client *Client) Insert(serialized []byte) {
	//client.stateHandler.InsertSerialized(serialized)
}

func (client *Client) SendToServer() {
}
