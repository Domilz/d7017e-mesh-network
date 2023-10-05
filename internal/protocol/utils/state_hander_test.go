package utils

import (
	"sort"
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
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     69,
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
		TagId:    "MOCKTAG",
		DeviceId: "321",
		Rssi:     69,
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
