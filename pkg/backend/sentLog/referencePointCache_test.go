package sentLog

import (
	"sync"
	"testing"

	structs "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/forms"
	"github.com/stretchr/testify/assert"
)

func TestInitReferencePointCache(t *testing.T) {
	rpCache := InitReferencePointCache()
	rpCacheMock := &ReferencePointCache{make(map[string]*structs.XYZ), sync.RWMutex{}}

	assert.Equal(t, rpCacheMock, rpCache)
}

func TestAddReferencePointAndPosition(t *testing.T) {
	rpCache := InitReferencePointCache()

	pos1 := &structs.XYZ{
		X: 10,
		Y: 11,
		Z: 13,
	}

	rpCache.AddReferencePointAndPosition("rp1", pos1)

	pos2 := &structs.XYZ{
		X: 20,
		Y: 21,
		Z: 23,
	}

	rpCache.AddReferencePointAndPosition("rp2", pos2)

	pos3 := &structs.XYZ{
		X: 30,
		Y: 31,
		Z: 33,
	}

	rpCache.AddReferencePointAndPosition("rp3", pos3)

	cacheMock := map[string]*structs.XYZ{
		"rp1": pos1,
		"rp2": pos2,
		"rp3": pos3,
	}

	assert.Equal(t, cacheMock, rpCache.cache)
}

func TestAddReferencePointAndPositionChangePosition(t *testing.T) {
	rpCache := InitReferencePointCache()

	pos1 := &structs.XYZ{
		X: 10,
		Y: 11,
		Z: 13,
	}

	rpCache.AddReferencePointAndPosition("rp1", pos1)

	pos2 := &structs.XYZ{
		X: 20,
		Y: 21,
		Z: 23,
	}

	rpCache.AddReferencePointAndPosition("rp2", pos2)

	pos3 := &structs.XYZ{
		X: 30,
		Y: 31,
		Z: 33,
	}

	rpCache.AddReferencePointAndPosition("rp3", pos3)

	pos4 := &structs.XYZ{
		X: 40,
		Y: 41,
		Z: 43,
	}

	rpCache.AddReferencePointAndPosition("rp3", pos4)

	cacheMock := map[string]*structs.XYZ{
		"rp1": pos1,
		"rp2": pos2,
		"rp3": pos4,
	}

	assert.Equal(t, cacheMock, rpCache.cache)
}

func TestGetXYZ(t *testing.T) {
	rpCache := InitReferencePointCache()

	pos1 := &structs.XYZ{
		X: 10,
		Y: 11,
		Z: 13,
	}

	rpCache.AddReferencePointAndPosition("rp1", pos1)

	pos, _ := rpCache.GetXYZ("rp1")

	assert.Equal(t, pos, pos1)
}

func TestGetXYZError(t *testing.T) {
	rpCache := InitReferencePointCache()

	pos1 := &structs.XYZ{
		X: 10,
		Y: 11,
		Z: 13,
	}

	rpCache.AddReferencePointAndPosition("rp1", pos1)

	_, err := rpCache.GetXYZ("rp")

	assert.Equal(t, "No such reference point id.", err.Error())
}

func TestPopulateWithMockedData(t *testing.T) {
	rpCache := InitReferencePointCache()
	rpCache.PopulateWithMockedData()
}
