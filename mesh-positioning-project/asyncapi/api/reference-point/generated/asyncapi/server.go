
package asyncapi


// GetAMQPURI return the AMQP URI.
//TODO this must be done using the async api server bindings    
func GetAMQPURI() string {
  //this must be passed in or created by the app based on the bindings
  return "amqp://guest:guest@localhost:5672/"
}
    
  