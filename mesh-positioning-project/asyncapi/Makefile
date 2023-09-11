define HELP_MESSAGE
Lorem ipsum
Lorem ipsum
endef
export HELP_MESSAGE


.PHONY: h help
h help: 
	@echo "$$HELP_MESSAGE";

APIS := $(shell find ./api -mindepth 2 -maxdepth 3 -regextype egrep -regex '.*ya?ml$$')

# https://www.asyncapi.com/docs/tools/generator/template
TEMPLATES := markdown-template html-template

.PHONY: generate
generate:
	$(info Found $(APIS))
	@for API in $(APIS); do \
		GEN_FOLDER=$$(dirname $$API)/generated; \
		for TEMPLATE in $(TEMPLATES); do \
			echo "Generating $${TEMPLATE} for $${API} to $$GEN_FOLDER"; \
			docker run --rm -it \
			-v $${API}:/app/asyncapi.yml \
			-v $$(dirname $$API)/generated:/app/output \
			emiinternal.azurecr.io/tool/asyncapi/generator:latest -o /app/output /app/asyncapi.yml @asyncapi/$${TEMPLATE} --force-write; \
		done; \
	done

.PHONY: playground
playground: 
	docker run -it --rm --name asyncapi-playground -p 5000:5000 emiinternal.azurecr.io/tool/asyncapi/playground:latest
