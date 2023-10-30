package debugglog

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestSaveAndGetDebuggLog(t *testing.T) {

	dbLDB := InitDebuggLogDatabase("database/DebuggLogDatabase.db")

	byteArray := []byte{97, 98, 99, 100, 101, 102}
	dbLDB.Save(byteArray)
	_, data := dbLDB.GetDebuggLog()
	assert.Equal(t, byteArray, data[len(data)-1])

}
