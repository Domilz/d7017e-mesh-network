package utils

import (
	"fmt"
	"strings"

	pb "github.com/Domilz/d7017e-mesh-network/internal/protocol/grpc_bidirectional_stream/protofiles/tag"
)

// PrintFormattedState prints a formatted State message with multiple Readings.
func PrintFormattedState(state *pb.State) {
	fmt.Println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
	fmt.Println("State:")
	for _, reading := range state.Readings {
		fmt.Printf("  TagID: %s\n", state.TagId)
		fmt.Println(FormatReading(reading))
		fmt.Println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
	}
}

// FormatReading formats a Reading message into a string.
func FormatReading(reading *pb.Reading) string {
	var builder strings.Builder
	builder.WriteString("Reading:\n")
	builder.WriteString(fmt.Sprintf("  TagID: %s\n", reading.TagId))
	builder.WriteString(fmt.Sprintf("  DeviceID: %s\n", reading.DeviceId))
	builder.WriteString(fmt.Sprintf("  RSSI: %d\n", reading.Rssi))
	builder.WriteString(fmt.Sprintf("  Timestamp: %v", reading.Ts))
	return builder.String()
}
