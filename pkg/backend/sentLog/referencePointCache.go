package sentLog

import (
	"errors"
	"sync"

	structs "github.com/Domilz/d7017e-mesh-network/pkg/backend/grpcServer/forms"
)

type ReferencePointCache struct {
	cache map[string]*structs.XYZ
	mutex sync.RWMutex
}

func InitReferencePointCache() *ReferencePointCache {
	rpCache := &ReferencePointCache{
		cache: make(map[string]*structs.XYZ),
	}

	return rpCache
}

func (rpCache *ReferencePointCache) AddReferencePointAndPosition(rp_id string, position *structs.XYZ) {
	rpCache.mutex.Lock()
	rpCache.cache[rp_id] = position
	rpCache.mutex.Unlock()
}

func (rpCache *ReferencePointCache) GetXYZ(rp_id string) (*structs.XYZ, error) {
	rpCache.mutex.Lock()
	position, keyExist := rpCache.cache[rp_id]
	rpCache.mutex.Unlock()

	if keyExist {
		return position, nil
	}

	return nil, errors.New("no such reference point id.")
}
func (rpCache *ReferencePointCache) GetAllReferencePoints() ([]string, []structs.XYZ) {
	rpCache.mutex.Lock()
	sArr := []string{}
	positions := []structs.XYZ{}
	for rpId, pos := range rpCache.cache {
		sArr = append(sArr, rpId)
		positions = append(positions, *pos)

	}
	rpCache.mutex.Unlock()
	return sArr, positions
}

func (rpCache *ReferencePointCache) PopulateWithMockedData() {

	rpCache.createBeacon("rpId1", -1, -4, 2)
	rpCache.createBeacon("rpId2", 4, 0, -1)
	rpCache.createBeacon("rpId3", 4, 2, -2)
	rpCache.createBeacon("rpId4", -2, 4, -3)
	rpCache.createBeacon("rpId5", 2, 1, 3)
	rpCache.createBeacon("rpId6", 2, -2, 1)
	rpCache.createBeacon("rpId7", -4, -2, 1)
	rpCache.createBeacon("rpId8", -2, 0, 2)
	rpCache.createBeacon("rpId9", -3, -4, -4)
	rpCache.createBeacon("rpId10", 3, -2, 0)
	rpCache.createBeacon("rpId11", -4, 1, 3)
	rpCache.createBeacon("rpId12", 0, -1, 3)
	rpCache.createBeacon("rpId13", -4, 0, -1)
	rpCache.createBeacon("rpId14", 3, -3, -2)
	rpCache.createBeacon("rpId15", 2, -1, 3)
	rpCache.createBeacon("rpId16", 0, 2, 0)
	rpCache.createBeacon("rpId17", -1, 2, 1)
	rpCache.createBeacon("rpId18", 4, 0, -4)
	rpCache.createBeacon("rpId19", -2, -1, 0)
	rpCache.createBeacon("rpId20", 2, 1, 1)
	rpCache.createBeacon("rpId21", 1, 4, 0)
	rpCache.createBeacon("rpId22", -4, 1, -2)
	rpCache.createBeacon("rpId23", -4, 1, -1)
	rpCache.createBeacon("rpId24", 0, 2, 2)
	rpCache.createBeacon("rpId25", -3, 2, 4)
	rpCache.createBeacon("rpId26", -1, -1, -4)

}
func (rpCache *ReferencePointCache) createBeacon(name string, x int, y int, z int) {
	pos := &structs.XYZ{
		X: (float32(x)),
		Y: (float32(y)),
		Z: (float32(z)),
	}
	rpCache.AddReferencePointAndPosition(name, pos)
}
