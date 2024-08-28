#清除之前定义的变量，确保每个模块的配置从干净的状态开始。
LOCAL_PATH:=$(call my-dir)

#清除之前定义的变量，确保每个模块的配置从干净的状态开始。
include $(CLEAR_VARS)

#定义了生成的动态链接库的名称为OBOJni，在Android系统中会自动添加前缀lib和后缀.so，成为libOBOJni.so
LOCAL_MODULE := OBOJni

#指定test.cpp为此动态库的源文件。
LOCAL_SRC_FILES := test.cpp

#指定链接器链接Android系统的日志库liblog，以便库中可以使用日志功能。
LOCAL_LDLIBS := -llog

#引入构建系统用来编译和链接生成动态链接库的规则和命令。
include $(BUILD_SHARED_LIBRARY)