ROOT_DIR=${PWD}

.DEFAULT_GOAL := help

# general
help:
	@echo "Type: make [rule]. Available options are:"
	@echo ""
	@echo "- help"
	@echo "- compile"
	@echo "- run"
	@echo "- submodule-update"
	@echo ""

compile:
	rm -rf dist
	mkdir -p dist
	cd dist && cmake .. && make

submodule-update:
	git submodule update --recursive --remote

run:
	make compile
	cd dist && ./epubreader