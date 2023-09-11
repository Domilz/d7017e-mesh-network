.PHONY: d docker
d docker: 
	docker build . -t emiinternal.azurecr.io/tool/asyncapi/generator:latest

.PHONY: p push
p push: 
	docker push emiinternal.azurecr.io/tool/asyncapi/generator:latest