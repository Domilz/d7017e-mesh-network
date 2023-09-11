
package asyncapi

import (
	"github.com/ThreeDotsLabs/watermill"
	"github.com/ThreeDotsLabs/watermill/message"
)

// GetRouter returns a watermill router. 
func GetRouter() (*message.Router, error){
	logger := watermill.NewStdLogger(false, false)
	return message.NewRouter(message.RouterConfig{}, logger)
}


// ConfigureAMQPSubscriptionHandlers configures the router with the subscription handler.    
func ConfigureAMQPSubscriptionHandlers(r *message.Router, s message.Subscriber) {

  r.AddNoPublisherHandler(
    "ReferencePointUpdateEventOperation",     // handler name, must be unique
    "v1.reference-point.event.update",         // topic from which we will read events
    s,
    ReferencePointUpdateEventOperation, 
  )

  r.AddNoPublisherHandler(
    "ReferencePointModifyResponseOperation",     // handler name, must be unique
    "v1.reference-point.response.operation.service.domain",         // topic from which we will read events
    s,
    ReferencePointModifyResponseOperation, 
  )

  r.AddNoPublisherHandler(
    "ReferencePointSnapshotResponseOperation",     // handler name, must be unique
    "v1.reference-point.response.snapshot.service.domain",         // topic from which we will read events
    s,
    ReferencePointSnapshotResponseOperation, 
  )

}    

  