
package asyncapi

import (
    "github.com/ThreeDotsLabs/watermill"
    "github.com/ThreeDotsLabs/watermill-amqp/pkg/amqp"
)


// GetAMQPPublisher returns an amqp publisher based on the URI
func GetAMQPPublisher(amqpURI string) (*amqp.Publisher, error) {
    amqpConfig := amqp.NewDurableQueueConfig(amqpURI)

    return amqp.NewPublisher(
        amqpConfig,
        watermill.NewStdLogger(false, false),
    )
}
    
    