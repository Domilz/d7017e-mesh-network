package debuglog

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestSaveAndGetDebugLog(t *testing.T) {

	dbLDB := InitDebugLogDatabase("database/DebugLogDatabase.db")

	byteArray := []byte{97, 98, 99, 100, 101, 102}
	dbLDB.Save(byteArray)
	_, data := dbLDB.GetDebugLog()
	assert.Equal(t, byteArray, data[len(data)-1])

}
