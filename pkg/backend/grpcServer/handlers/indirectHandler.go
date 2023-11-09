package handlers

import (
	"fmt"

	structs "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/forms"
	sentLog "github.com/Domilz/d7017e-mesh-network/pkg/backend/sentLog"
	pb "github.com/Domilz/d7017e-mesh-network/pkg/protocol/protofiles/tag"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type IndirectHandler struct {
	sentLog *sentLog.SentLogServer
	rpCache *ReferencePointCache
}

func InitIndirectHandler(sLog *sentLog.SentLogServer) *IndirectHandler {
	indirectHandler := &IndirectHandler{}
	indirectHandler.sentLog = sLog
	rpCache := InitReferencePointCache()
	rpCache.PopulateWithMockedData()
	indirectHandler.rpCache = rpCache

	return indirectHandler
}

func (indirectHanddler *IndirectHandler) FillOutAndSendForm(reading *pb.Reading) {
	createTime := &structs.Time{
		Seconds: int(timestamppb.Now().Seconds), //Change later.
		Nanos:   int(timestamppb.Now().Nanos),   //Change later.
	}

	updateTime := &structs.Time{
		Seconds: int(timestamppb.Now().Seconds), //Change later.
		Nanos:   int(timestamppb.Now().Nanos),   //Change later.
	}

	timestampTime := &structs.Time{
		Seconds: int(timestamppb.Now().Seconds),
		Nanos:   int(timestamppb.Now().Nanos),
	}

	newLocation := indirectHanddler.estimatePosition(reading.RpId)

	newErr := &structs.Error{ //Change later.
		Code:    0,
		Message: "",
	}

	newWifiProps := &structs.WifiProperties{
		Online:         true,     //Change later.
		LocationStatus: "Normal", //Change later.
		IP:             "192.168.0.1",
	}

	newBleProps := &structs.BLEProperties{
		Active:            true, //Change later.
		BatteryPercentage: 100,  //Change later.
	}

	newLtePops := &structs.LteProperties{}

	newVendorProps := &structs.VendorProperties{}

	newOperands := &structs.Operands{
		Uuid:             "", //Change later.
		RpId:             reading.RpId,
		Name:             "", //Change later.
		Description:      "", //Change later.
		CreateTime:       *createTime,
		UpdateTime:       *updateTime,
		Location:         *newLocation,
		Type:             "", //Change later.
		WifiProperties:   *newWifiProps,
		BLEProperties:    *newBleProps,
		LteProperties:    *newLtePops,
		Vendor:           "", //Change later.
		VendorProperties: *newVendorProps,
	}

	newReferencePointForm := &structs.ReferencePointForm{
		Timestamp:     *timestampTime,
		CorrelationId: "",     //Change later.
		Operation:     "POST", //Change later.
		Operands:      []structs.Operands{*newOperands},
		Error:         *newErr,
	}

	indirectHanddler.sendFormToCentral(newReferencePointForm)
}

// Should send to api but don't have access so sending to log.
func (indirectHandler *IndirectHandler) sendFormToCentral(rpForm *structs.ReferencePointForm) {
	indirectHandler.sentLog.SaveReferencePointForm(*rpForm)
}

// Not finished, need the position estimation algorithm.
func (indirect *IndirectHandler) estimatePosition(rpId string) *structs.XYZ {

	beaconPosition, err := indirect.rpCache.GetXYZ(rpId)

	if err != nil {
		return &structs.XYZ{}
	}

	//Use the position estimation algorithm. Using print so the program does not complain.
	fmt.Print(beaconPosition)

	estimatedPosition := &structs.XYZ{
		X: 0,
		Y: 0,
		Z: 0,
	}

	return estimatedPosition
}
