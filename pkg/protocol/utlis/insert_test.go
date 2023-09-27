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

	mockState := &pb.State{
		TagId: "666",
		Readings: map[string]*pb.Reading{
			mockReading.TagId: mockReading,
		},
	}

	InsertSingleReading(mockState, mockReading2)

	expectedMockState := &pb.State{
		TagId: "666",
		Readings: map[string]*pb.Reading{
			mockReading.TagId:  mockReading,
			mockReading2.TagId: mockReading2,
		},
	}

	assert.Equal(t, expectedMockState, mockState)

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

	mockState := &pb.State{
		TagId: "666",
		Readings: map[string]*pb.Reading{
			mockReading.TagId: mockReading,
		},
	}

	InsertSingleReading(mockState, mockReading2)

	assert.Equal(t, time, mockState.Readings[mockReading.TagId].Ts.Seconds)

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

	mockState := &pb.State{
		TagId: "666",
		Readings: map[string]*pb.Reading{
			mockReading.TagId: mockReading,
		},
	}

	InsertSingleReading(mockState, mockReading2)

	assert.Equal(t, time2, mockState.Readings[mockReading.TagId].Ts.Seconds)

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

	mockState := &pb.State{
		TagId: "666",
		Readings: map[string]*pb.Reading{
			mockReading.TagId:  mockReading,
			mockReading2.TagId: mockReading2,
		},
	}

	mockState2 := &pb.State{
		TagId: "669",
		Readings: map[string]*pb.Reading{
			mockReading3.TagId: mockReading3,
		},
	}

	InsertMultipleReadings(mockState.Readings, mockState2)

	expectedMockState := &pb.State{
		TagId: "669",
		Readings: map[string]*pb.Reading{
			mockReading3.TagId: mockReading3,
			mockReading.TagId:  mockReading,
			mockReading2.TagId: mockReading2,
		},
	}

	assert.Equal(t, expectedMockState, mockState2)

}
