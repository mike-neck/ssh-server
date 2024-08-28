optionVersion := $(OPT_VER)
imageCountVersion := $(shell docker images --format json | jq 'select(.Tag | test("openjdk\\-ssh"; "i")) | 1' | wc -l | bc)
newTagVersion := $(firstword $(optionVersion) $(imageCountVersion))

optionImageName := $(IMAGE_NAME)
imageName := $(firstword $(optionImageName) ssh-server)

optionPlatform := $(PLATFORM)
platform := $(shell uname -o | tr '[:upper:]' '[:lower:]')/$(shell uname -m)
platformName := $(firstword $(optionPlatform) $(platform))

.PHONY: test-options
test-options:
	@echo $(newTagVersion)
	@echo $(imageName)
	@echo $(platformName)

.PHONY: build
build:
	@./gradlew distTar
	@docker buildx build --platform "$(platformName)" -t "$(imageName):$(newTagVersion)" .
	@echo docker push "$(imageName):$(newTagVersion)"
