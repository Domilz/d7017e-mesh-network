package handlers

import (
	"errors"
	"sync"

	structs "github.com/Domilz/d7017e-mesh-network/internal/backend/grpcServer/forms"
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

	return nil, errors.New("No such reference point id.")
}