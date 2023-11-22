package handlers

import (
	structs "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/forms"
	sentLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/sentLog"
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type DirectHandler struct {
	sentLog *sentLog.SentLogServer
}

func InitDirectHandler(sLog *sentLog.SentLogServer) *DirectHandler {
	directHandler := &DirectHandler{sLog}
	return directHandler
}

func (directHandler *DirectHandler) FillOutAndSendForm(reading *pb.Reading) {
	timeForSentRecived := &structs.Time{
		Seconds: int(timestamppb.Now().Seconds),
		Nanos:   int(timestamppb.Now().Nanos),
	}

	newChainDelayStruct := &structs.ChainDelay{
		Name:     "tagbackend",
		Sent:     *timeForSentRecived,
		Received: *timeForSentRecived,
	}

	newReadingStruct := &structs.Reading{
		Rp_id:       reading.RpId,
		Rssi:        int(reading.Rssi),
		Tag_id:      reading.TagId,
		Type:        "BLE",
		Chain_delay: []structs.ChainDelay{*newChainDelayStruct},
	}

	newRssiForm := &structs.RssiForm{
		Readings: []structs.Reading{*newReadingStruct},
	}

	directHandler.sendFormToCentral(newRssiForm)
}

// Should send to api but don't have access so sending to log instead when
// it is created. Using print so the system does not complain.
func (directHandler *DirectHandler) sendFormToCentral(rssiForm *structs.RssiForm) {
	directHandler.sentLog.SaveRssiForm(*rssiForm)
}
