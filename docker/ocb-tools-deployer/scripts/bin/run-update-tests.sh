#!/bin/bash

cd ${OCB_USER_HOME}/opencat-business
generate_settings.sh

ocb-test.sh run --application Update --summary --remote --config settings
cd -

cd ${OCB_USER_HOME}
mkdir -p results

cp opencat-business/*.log results/.
cp opencat-business/target/surefire-reports/TEST-*.xml results/.
