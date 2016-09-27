#!/bin/bash

cd ${OCB_USER_HOME}/opencat-business
generate_settings.sh

ocb-test.sh run --application Update --summary --remote --config settings
cd -

cp ${OCB_USER_HOME}/opencat-business/*.log results/.
cp ${OCB_USER_HOME}/opencat-business/target/surefire-reports/TEST-*.xml results/.

cd -
