cmake_minimum_required(VERSION 3.10)

# project variables
set(PROJECT_NAME "epubreader")
set(CMAKE_CXX_STANDARD 11)

# project source files
set(
    SRC
    thirdparty/civetweb/src/civetweb.c
    thirdparty/civetweb/src/CivetServer.cpp
    dependency-server/build/source/shared/EpubHandler.cpp
    dependency-server/build/source/cpp/EpubReaderServerImpl.cpp
    main.cpp
)

include_directories(
    thirdparty/civetweb/include
    dependency-server/build/source/shared
    dependency-server/build/source/cpp
    dependency-server/build/djinni/cpp-output
)

# setup project name
project(${PROJECT_NAME})

# setup compile process
add_executable(
    ${PROJECT_NAME}
    ${SRC}
)