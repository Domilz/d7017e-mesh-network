package handlers

import (
	structs "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/forms"
	sentLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/sentLog"
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type IndirectHandler struct {
	sentLog *sentLog.SentLogServer
	rpCache *sentLog.ReferencePointCache
}

func InitIndirectHandler(sLog *sentLog.SentLogServer) *IndirectHandler {
	indirectHandler := &IndirectHandler{}
	indirectHandler.sentLog = sLog
	rpCache := sentLog.InitReferencePointCache()
	rpCache.PopulateWithMockedData()
	indirectHandler.rpCache = rpCache

	return indirectHandler
}

func (indirectHanddler *IndirectHandler) FillOutAndSendForm(reading *pb.Reading) {
	sentTime := &structs.Time{
		Seconds: int(timestamppb.Now().Seconds), //Change later.
		Nanos:   int(timestamppb.Now().Nanos),   //Change later.
	}

	receivedTime := &structs.Time{
		Seconds: int(timestamppb.Now().Seconds), //Change later.
		Nanos:   int(timestamppb.Now().Nanos),   //Change later.
	}

	newChainDelayStruct := &structs.ChainDelay{
		Name:     "tagbackend",
		Sent:     *sentTime,
		Received: *receivedTime,
	}

	newLocation, acc := indirectHanddler.estimatePosition(reading.RpId, reading.Rssi)

	xyzForm := &structs.XYZForm{
		X:           float32(newLocation.X),
		Y:           float32(newLocation.Y),
		Z:           float32(newLocation.Z),
		Accuracy:    int(acc),
		Tag_id:      reading.TagId,
		Chain_delay: []structs.ChainDelay{*newChainDelayStruct},
	}

	indirectHanddler.sendFormToCentral(xyzForm)
}

// Should send to api but don't have access so sending to log.
func (indirectHandler *IndirectHandler) sendFormToCentral(xyzForm *structs.XYZForm) {
	indirectHandler.sentLog.SaveXYZForm(*xyzForm)
}

// This is a naive approach. The algorithm estimates the position of the indirect tag based on the last seen reference point.
// TODO: Improve
func (indirect *IndirectHandler) estimatePosition(rpId string, rssi int32) (*structs.XYZ, int) {

	beaconPosition, err := indirect.rpCache.GetXYZ(rpId)

	if err != nil {
		return &structs.XYZ{}, 0
	}
	accuracy := 5 //

	return beaconPosition, accuracy
}
