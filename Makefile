ROOT_DIR=${PWD}

.DEFAULT_GOAL := help

# general
help:
	@echo "Type: make [rule]. Available options are:"
	@echo ""
	@echo "- help"
	@echo "- build"
	@echo "- run"
	@echo "- submodule-update"
	@echo ""

build:
	rm -rf dist
	mkdir -p dist
	cd dist && cmake .. && make

submodule-update:
	git submodule update --recursive --remote

run:
	make build
	cd dist && ./epubreader