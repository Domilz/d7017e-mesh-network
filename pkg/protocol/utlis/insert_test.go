package utlis

import (
	"testing"

	"github.com/Domilz/d7017e-mesh-network/pkg/protocol/pb"
	"github.com/golang/protobuf/ptypes/timestamp"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func TestInsertSingleReading(t *testing.T) {

	mockReading := &pb.Reading{
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading2 := &pb.Reading{
		TagId:    "MOCKTAG2",
		DeviceId: "3212",
		Rssi:     13,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	sh := StateHandler{}
	sh.initStateHandler("666")
	sh.InsertSingleReading(mockReading)
	sh.InsertSingleReading(mockReading2)

	expectedMockState := &pb.State{
		TagId:    "666",
		Readings: []*pb.Reading{mockReading, mockReading2},
	}

	assert.Equal(t, expectedMockState, sh.getState())

}

func TestLessTimeDontInsertSingleReading(t *testing.T) {

	time := timestamppb.Now().Seconds
	time2 := time / 100

	mockReading := &pb.Reading{
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     69,
		Ts: &timestamp.Timestamp{
			Seconds: time,
		},
	}

	mockReading2 := &pb.Reading{
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     10,
		Ts: &timestamp.Timestamp{
			Seconds: time2,
		},
	}

	sh := StateHandler{}
	sh.initStateHandler("666")
	sh.InsertSingleReading(mockReading)
	sh.InsertSingleReading(mockReading2)
	reading := sh.getReading(mockReading.TagId)

	assert.Equal(t, time, reading.Ts.Seconds)

}

func TestMoreTimeInsertSingleReading(t *testing.T) {

	time := timestamppb.Now().Seconds
	time2 := time * 100

	mockReading := &pb.Reading{
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     69,
		Ts: &timestamp.Timestamp{
			Seconds: time,
		},
	}

	mockReading2 := &pb.Reading{
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     10,
		Ts: &timestamp.Timestamp{
			Seconds: time2,
		},
	}

	sh := StateHandler{}
	sh.initStateHandler("666")

	sh.InsertSingleReading(mockReading)
	sh.InsertSingleReading(mockReading2)
	reading := sh.getReading(mockReading.TagId)
	assert.Equal(t, time2, reading.Ts.Seconds)

}

func TestInsertMultipleReading(t *testing.T) {

	mockReading := &pb.Reading{
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading2 := &pb.Reading{
		TagId:    "MOCKTAG2",
		DeviceId: "3212",
		Rssi:     13,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading3 := &pb.Reading{
		TagId:    "MOCKTAG3",
		DeviceId: "3213",
		Rssi:     10,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}
	mockReading4 := &pb.Reading{
		TagId:    "MOCKTAG4",
		DeviceId: "3214",
		Rssi:     10,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockState := &pb.State{
		TagId:    "666",
		Readings: []*pb.Reading{mockReading, mockReading2},
	}

	mockState2 := &pb.State{
		TagId:    "669",
		Readings: []*pb.Reading{mockReading3, mockReading4},
	}

	sh := StateHandler{}
	sh.initStateHandler("669")
	sh.InsertMultipleReadings(mockState)
	sh.InsertMultipleReadings(mockState2)
	expectedMockState := &pb.State{
		TagId:    "669",
		Readings: []*pb.Reading{mockReading, mockReading2, mockReading3, mockReading4},
	}
	actualState := sh.getState()
	assert.Equal(t, expectedMockState, actualState)

}
