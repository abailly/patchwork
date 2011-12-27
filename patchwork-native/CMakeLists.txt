cmake_minimum_required(VERSION 2.8)
project(SimpleProfilingAgent)

FILE(GLOB SRCS src/*.c)

set(JAVA_HOME "c:/jdk1.6.0_06/")

# include Java headers
INCLUDE_DIRECTORIES(${JAVA_HOME}/include)
INCLUDE_DIRECTORIES(${JAVA_HOME}/include/win32)
INCLUDE_DIRECTORIES(include/)

# java libs
file (
     	GLOB_RECURSE
	JAVA_LIBS
	${JAVA_HOME}/lib/*.lib
)

ADD_LIBRARY(PatchworkAgent SHARED ${SRCS})

TARGET_LINK_LIBRARIES(PatchworkAgent
  ${JAVA_LIBS}
  Ws2_32 WINMM
  )

