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

	return nil, errors.New("No such reference point id.")
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
	/*
		for i := 1; i < 27; i++ {

			c := rune(i + 96)
			x := string(c) + string(c)
			key := x + ":" + x + ":" + x + ":" + x + ":" + x + ":" + x
			pos := &structs.XYZ{
				X: (float32(i)),
				Y: (float32(i)),
				Z: (float32(i)),
			}
			rpCache.AddReferencePointAndPosition(key, pos)

		}
	*/
	rpCache.createBeacon("aa:aa:aa:aa:aa:aa", -1, -4, 2)
	rpCache.createBeacon("bb:bb:bb:bb:bb:bb", 4, 0, -1)
	rpCache.createBeacon("cc:cc:cc:cc:cc:cc", 4, 2, -2)
	rpCache.createBeacon("dd:dd:dd:dd:dd:dd", -2, 4, -3)
	rpCache.createBeacon("ee:ee:ee:ee:ee:ee", 2, 1, 3)
	rpCache.createBeacon("ff:ff:ff:ff:ff:ff", 2, -2, 1)
	rpCache.createBeacon("gg:gg:gg:gg:gg:gg", -4, -2, 1)
	rpCache.createBeacon("hh:hh:hh:hh:hh:hh", -2, 0, 2)
	rpCache.createBeacon("ii:ii:ii:ii:ii:ii", -3, -4, -4)
	rpCache.createBeacon("jj:jj:jj:jj:jj:jj", 3, -2, 0)
	rpCache.createBeacon("kk:kk:kk:kk:kk:kk", -4, 1, 3)
	rpCache.createBeacon("ll:ll:ll:ll:ll:ll", 0, -1, 3)
	rpCache.createBeacon("mm:mm:mm:mm:mm:mm", -4, 0, -1)
	rpCache.createBeacon("nn:nn:nn:nn:nn:nn", 3, -3, -2)
	rpCache.createBeacon("oo:oo:oo:oo:oo:oo", 2, -1, 3)
	rpCache.createBeacon("pp:pp:pp:pp:pp:pp", 0, 2, 0)
	rpCache.createBeacon("qq:qq:qq:qq:qq:qq", -1, 2, 1)
	rpCache.createBeacon("rr:rr:rr:rr:rr:rr", 4, 0, -4)
	rpCache.createBeacon("ss:ss:ss:ss:ss:ss", -2, -1, 0)
	rpCache.createBeacon("tt:tt:tt:tt:tt:tt", 2, 1, 1)
	rpCache.createBeacon("uu:uu:uu:uu:uu:uu", 1, 4, 0)
	rpCache.createBeacon("vv:vv:vv:vv:vv:vv", -4, 1, -2)
	rpCache.createBeacon("ww:ww:ww:ww:ww:ww", -4, 1, -1)
	rpCache.createBeacon("xx:xx:xx:xx:xx:xx", 0, 2, 2)
	rpCache.createBeacon("yy:yy:yy:yy:yy:yy", -3, 2, 4)
	rpCache.createBeacon("zz:zz:zz:zz:zz:zz", -1, -1, -4)

}
func (rpCache *ReferencePointCache) createBeacon(name string, x int, y int, z int) {
	pos := &structs.XYZ{
		X: (float32(x)),
		Y: (float32(y)),
		Z: (float32(z)),
	}
	rpCache.AddReferencePointAndPosition(name, pos)
}
