cmake_minimum_required(VERSION 3.10)
project(reqrio-qt)

add_compile_options(/utf-8)
set(CMAKE_AUTOMOC ON)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_PREFIX_PATH D:/softwares/qt/5.15.2/msvc2019_64)
find_package(QT NAMES Qt6 Qt5 REQUIRED COMPONENTS Core)
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Core)
add_library(reqrio-qt STATIC bindings.h
        Session.cpp
        Session.h
        Response.cpp
        Response.h
        Cookie.cpp
        Cookie.h
        Timeout.cpp
        Timeout.h
        WebSocket.cpp
        WebSocket.h
        Url.cpp
        Url.h
        util.h
        util.cpp
        Body.cpp
        Body.h
        Fingerprint.cpp
        Fingerprint.h
        CipherSuite.h
        Algorithm.h
        CompressionMethod.h
        Group.h
        Version.h
        EcPointFormat.h
        ExtensionType.h
        H2SettingType.h)

link_directories(${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(reqrio-qt PUBLIC Qt${QT_VERSION_MAJOR}::Core reqrio)
include(GNUInstallDirs)
add_executable(test_reqrio test.cpp)
target_link_libraries(test_reqrio reqrio-qt)