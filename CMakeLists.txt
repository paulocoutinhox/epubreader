cmake_minimum_required(VERSION 3.10)

# project variables
set(PROJECT_NAME "epubreader")

# project source files
set(
    SRC
    ../../cpp/civetweb/src/civetweb.c
    ../../cpp/civetweb/src/CivetServer.cpp
    main.cpp
)

include_directories(
    ../../cpp/civetweb/include
)

# setup project name
project(${PROJECT_NAME})

# setup compile process
add_executable(
    ${PROJECT_NAME}
    ${SRC}
)