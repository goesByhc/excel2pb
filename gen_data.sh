#!/bin/bash

# 确认有JAVA环境
# JDK: https://www.oracle.com/java/technologies/javase-downloads.html

# 确认protobuf环境 版本3.13.0
# protoc --version
# brew install protobuf@3.13.0

# 确认Go环境
# go安装包: https://golang.org/dl/
# go get github.com/golang/protobuf/proto
# go get github.com/golang/protobuf/protoc-gen-go

# 确认有NodeJS环境
# brew install -g node
# 确认安装pbjs pbts
# npm install -g protobufjs


function echo_color() {
    if [ $1 == "green" ]; then
        echo "\033[32;40m$2\033[0m"
    elif [ $1 == "red" ]; then
        echo "\033[31;40m$2\033[0m"
    fi
}

script_path=`dirname "$0"`
echo $script_path

BAST_CLASSPATH=$script_path/excel2pb/lib
CLASSPATH=$BAST_CLASSPATH/dom4j-1.6.1.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/poi-3.7.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/poi-ooxml-3.7.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/geronimo-stax-api_1.0_spec-1.0.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/poi-ooxml-schemas-3.7.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/protobuf-java-3.13.0.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/stax-api-1.0.1.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/xml-apis-1.0.b2.jar
CLASSPATH=$CLASSPATH:$BAST_CLASSPATH/xmlbeans-2.3.0.jar
CLASSPATH=$CLASSPATH:$script_path/excel2pb/bin

binpath=$script_path/excel2pb/bin/
if [[ ! -d $binpath ]]; then
  mkdir $binpath
fi

echo_color green $script_path
echo_color green $CLASSPATH

xlsxPath=$1
archiveFolder=$2
targetProtoFolder=$script_path/../DataProto

rm -rf $targetProtoFolder
mkdir $targetProtoFolder

mkdir $archiveFolder

javac -Xlint:unchecked -classpath $CLASSPATH -d $binpath -sourcepath $script_path/excel2pb/src/ $script_path/excel2pb/src/com/inke/pb/*.java
if [[ $? -ne 0 ]]; then
    exit 1
fi

echo_color green 'create proto'
java  -Xmx1024m -Djava.awt.headless=true -classpath $CLASSPATH com.inke.pb.BootStrap file $xlsxPath $targetProtoFolder/ > $script_path/gen_data.log
if [[ $? -ne 0 ]]; then
    exit 1
fi

echo_color green 'generating classes files'
protoc $targetProtoFolder/Config*.proto --proto_path=$targetProtoFolder --java_out=$script_path/excel2pb/src
if [[ $? -ne 0 ]]; then
    exit 1
fi

echo_color green 'compiling Data'
javac -Xlint:unchecked -classpath $CLASSPATH -d $binpath -sourcepath $script_path/excel2pb/src $script_path/excel2pb/src/com/inke/conf/*.java
if [[ $? -ne 0 ]]; then
    exit 1
fi

echo_color green 'generating GameConfig file'
java -Xmx512m -classpath $CLASSPATH com.inke.pb.BootStrap data  $xlsxPath $archiveFolder/ > $script_path/GameConfig.txt
if [[ $? -ne 0 ]]; then
    exit 1
fi

echo_color green "done [gen_data]"
