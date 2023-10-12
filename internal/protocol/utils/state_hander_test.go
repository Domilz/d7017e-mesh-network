package utils

import (
	"sort"
	"strconv"
	"sync"
	"testing"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/protofiles/tag"
	"github.com/golang/protobuf/ptypes/timestamp"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func TestInnitStateHandler(t *testing.T) {
	sh := StateHandler{}
	sh.initStateHandler("666")

	shMock := StateHandler{"666", make(map[string]*pb.Reading), sync.RWMutex{}}
	assert.Equal(t, sh, shMock)

}
func TestGetReading(t *testing.T) {

	mockReading := &pb.Reading{
		TagId: "MOCKTAG",
		RpId:  "321",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}
	sh := StateHandler{}
	sh.initStateHandler("666")
	sh.InsertSingleReading(mockReading)

	assert.Equal(t, sh.getReading(mockReading.TagId), mockReading)

}

func TestMultipleGetReading(t *testing.T) {

	mockReading := &pb.Reading{
		TagId: "MOCKTAG",
		RpId:  "321",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading2 := &pb.Reading{
		TagId: "MOCKTAG2",
		RpId:  "3212",
		Rssi:  13,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading3 := &pb.Reading{
		TagId: "MOCKTAG3",
		RpId:  "3213",
		Rssi:  10,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading4 := &pb.Reading{
		TagId: "MOCKTAG4",
		RpId:  "3214",
		Rssi:  10,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	sh := StateHandler{}
	sh.initStateHandler("666")
	sh.InsertSingleReading(mockReading)
	sh.InsertSingleReading(mockReading2)
	sh.InsertSingleReading(mockReading3)
	sh.InsertSingleReading(mockReading4)
	assert.Equal(t, sh.getReading(mockReading.TagId), mockReading)
	assert.Equal(t, sh.getReading(mockReading2.TagId), mockReading2)
	assert.Equal(t, sh.getReading(mockReading3.TagId), mockReading3)
	assert.Equal(t, sh.getReading(mockReading4.TagId), mockReading4)
}

func TestGetState(t *testing.T) {
	mockReading := &pb.Reading{
		TagId: "MOCKTAG",
		RpId:  "321",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockState := &pb.State{
		TagId:    "666",
		Readings: []*pb.Reading{mockReading},
	}

	sh := StateHandler{}
	sh.initStateHandler("666")
	sh.InsertSingleReading(mockReading)
	assert.Equal(t, sh.getState(), mockState)

}

func TestMultipleGetState(t *testing.T) {

	mockReading := &pb.Reading{
		TagId: "MOCKTAG",
		RpId:  "321",
		Rssi:  69,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading2 := &pb.Reading{
		TagId: "MOCKTAG2",
		RpId:  "3212",
		Rssi:  13,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading3 := &pb.Reading{
		TagId: "MOCKTAG3",
		RpId:  "3213",
		Rssi:  10,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	mockReading4 := &pb.Reading{
		TagId: "MOCKTAG4",
		RpId:  "3214",
		Rssi:  10,
		Ts: &timestamp.Timestamp{
			Seconds: timestamppb.Now().Seconds,
		},
	}

	expectedMockState := &pb.State{
		TagId:    "666",
		Readings: []*pb.Reading{mockReading, mockReading2, mockReading3, mockReading4},
	}

	sh := StateHandler{}
	sh.initStateHandler("666")
	sh.InsertSingleReading(mockReading)
	sh.InsertSingleReading(mockReading2)
	sh.InsertSingleReading(mockReading3)
	sh.InsertSingleReading(mockReading4)
	actualState := sh.getState()
	sort.SliceStable(actualState.Readings, func(i, j int) bool {
		return actualState.Readings[i].TagId < actualState.Readings[j].TagId
	})
	assert.Equal(t, expectedMockState, actualState)
}

func TestGetStatesReadingLimit(t *testing.T) {

	mockReadings := generateMockReading(9)

	mockStates := []*pb.State{
		{TagId: "666", Readings: []*pb.Reading{mockReadings[0], mockReadings[1], mockReadings[2], mockReadings[3]}},
		{TagId: "666", Readings: []*pb.Reading{mockReadings[4], mockReadings[5], mockReadings[6], mockReadings[7]}},
		{TagId: "666", Readings: []*pb.Reading{mockReadings[8]}},
	}

	sh := StateHandler{}
	sh.initStateHandler("666")
	sh.InsertMultipleReadings(&pb.State{TagId: "666", Readings: mockReadings})
	actualStates := sh.getStatesReadingLimit(4)
	assert.Equal(t, mockStates[0], actualStates[0])
	assert.Equal(t, mockStates[1], actualStates[1])
	assert.Equal(t, mockStates[2], actualStates[2])

}

// For testing
func generateMockReading(count int) []*pb.Reading {
	var readings []*pb.Reading
	for i := 1; i <= count; i++ {
		mockReading := &pb.Reading{
			TagId: "MOCKTAG" + strconv.Itoa(i),
			RpId:  "00" + strconv.Itoa(i),
			Rssi:  10,
			Ts: &timestamp.Timestamp{
				Seconds: timestamppb.Now().Seconds,
			},
		}
		readings = append(readings, mockReading)
	}
	return readings

}
