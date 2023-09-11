
package asyncapi

import (
    "github.com/ThreeDotsLabs/watermill"
    "github.com/ThreeDotsLabs/watermill-amqp/pkg/amqp"
)


// GetAMQPSubscriber returns an amqp subscriber based on the URI   
func GetAMQPSubscriber(amqpURI string) (*amqp.Subscriber, error) {
    amqpConfig := amqp.NewDurableQueueConfig(amqpURI)

    return amqp.NewSubscriber(
        amqpConfig,
        watermill.NewStdLogger(false, false),
    )
}
    
    