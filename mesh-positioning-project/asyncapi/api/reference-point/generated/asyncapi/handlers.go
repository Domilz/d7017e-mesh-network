
package asyncapi

import (
	"log"
  
  "context"
  "github.com/ThreeDotsLabs/watermill-amqp/pkg/amqp"

  "encoding/json"
  "github.com/ThreeDotsLabs/watermill/message"
)

// ReferencePointUpdateEventOperation subscription handler for v1.reference-point.event.update.
func ReferencePointUpdateEventOperation(msg *message.Message) error {
    log.Printf("received message payload: %s", string(msg.Payload))

    var lm ReferencePointUpdateEvent
    err := json.Unmarshal(msg.Payload, &lm)
    if err != nil {
        log.Printf("error unmarshalling message: %s, err is: %s", msg.Payload, err)
    }
    return nil
}

// ReferencePointModifyResponseOperation subscription handler for v1.reference-point.response.operation.service.domain.
func ReferencePointModifyResponseOperation(msg *message.Message) error {
    log.Printf("received message payload: %s", string(msg.Payload))

    var lm ReferencePointModifyResponse
    err := json.Unmarshal(msg.Payload, &lm)
    if err != nil {
        log.Printf("error unmarshalling message: %s, err is: %s", msg.Payload, err)
    }
    return nil
}

// ReferencePointSnapshotResponseOperation subscription handler for v1.reference-point.response.snapshot.service.domain.
func ReferencePointSnapshotResponseOperation(msg *message.Message) error {
    log.Printf("received message payload: %s", string(msg.Payload))

    var lm ReferencePointUpdateEvent
    err := json.Unmarshal(msg.Payload, &lm)
    if err != nil {
        log.Printf("error unmarshalling message: %s, err is: %s", msg.Payload, err)
    }
    return nil
}


// ReferencePointPostCmdOperation is the publish handler for v1.reference-point.cmd.post.
func ReferencePointPostCmdOperation(ctx context.Context, a *amqp.Publisher, payload ReferencePointModifyCmd) error {
  m, err := PayloadToMessage(payload)
  if err != nil {
      log.Fatalf("error converting payload: %+v to message error: %s", payload, err)
  }

  return a.Publish("v1.reference-point.cmd.post", m)
}

// ReferencePointPutCmdOperation is the publish handler for v1.reference-point.cmd.put.
func ReferencePointPutCmdOperation(ctx context.Context, a *amqp.Publisher, payload ReferencePointModifyCmd) error {
  m, err := PayloadToMessage(payload)
  if err != nil {
      log.Fatalf("error converting payload: %+v to message error: %s", payload, err)
  }

  return a.Publish("v1.reference-point.cmd.put", m)
}

// ReferencePointPatchCmdOperation is the publish handler for v1.reference-point.cmd.patch.
func ReferencePointPatchCmdOperation(ctx context.Context, a *amqp.Publisher, payload ReferencePointModifyCmd) error {
  m, err := PayloadToMessage(payload)
  if err != nil {
      log.Fatalf("error converting payload: %+v to message error: %s", payload, err)
  }

  return a.Publish("v1.reference-point.cmd.patch", m)
}

// ReferencePointDeleteCmdOperation is the publish handler for v1.reference-point.cmd.delete.
func ReferencePointDeleteCmdOperation(ctx context.Context, a *amqp.Publisher, payload ReferencePointModifyCmd) error {
  m, err := PayloadToMessage(payload)
  if err != nil {
      log.Fatalf("error converting payload: %+v to message error: %s", payload, err)
  }

  return a.Publish("v1.reference-point.cmd.delete", m)
}

// ReferencePointSnapshotCmdOperation is the publish handler for v1.reference-point.cmd.snapshot.
func ReferencePointSnapshotCmdOperation(ctx context.Context, a *amqp.Publisher, payload ReferencePointSnapshotCmd) error {
  m, err := PayloadToMessage(payload)
  if err != nil {
      log.Fatalf("error converting payload: %+v to message error: %s", payload, err)
  }

  return a.Publish("v1.reference-point.cmd.snapshot", m)
}

