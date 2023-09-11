# AsyncAPI

Tools & POC for documentation of asynchronous APIs
Official documentation: https://www.asyncapi.com/

## Quick start

### Playground
`make playground`

> Live editor for HTML & Markdown template output.
> 
> Official documentation: https://github.com/asyncapi/playground

### Generator

`make generate`

> Generates markdown and html files in `/generated`  
>
> Official documentation: https://www.asyncapi.com/tools/generator

## How-to update tools

### Playground
1. Build docker as defined in official docs https://github.com/asyncapi/playground.
2. Tag the build docker image with  
`docker tag asyncapi-playground:latest emiinternal.azurecr.io/tool/asyncapi/playground:latest`
3. Push the image with  
`docker push emiinternal.azurecr.io/tool/asyncapi/playground:latest`

### Generator
1. Goto tool/generator
1. Run `make docker` 
1. Run `make push`

## TODO
- Build multiple files
- Where to host?
  - Pages?
  - Centralized / per project?
  - In applications, customer facing?
- Parameterized per installation?
