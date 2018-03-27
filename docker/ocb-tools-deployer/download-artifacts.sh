#!/bin/bash

ARTIFACTS_DIR=artifacts

OCB_TOOLS_ARTIFACT_URL=https://is.dbc.dk/job/updateservice/job/ocb-tools/job/master/lastSuccessfulBuild/artifact/target/dist/ocb-tools-1.0.0.tar.gz
OPENCAT_BUSINESS_ARTIFACT_URL=https://is.dbc.dk/job/updateservice/job/opencat-business/job/master/lastSuccessfulBuild/artifact/deploy/opencat-business.tar.gz

if [ -d "$ARTIFACTS_DIR" ]; then
    rm -rf $ARTIFACTS_DIR
fi

mkdir $ARTIFACTS_DIR

cd $ARTIFACTS_DIR
wget $OCB_TOOLS_ARTIFACT_URL $ARTIFACTS_DIR/.
wget $OPENCAT_BUSINESS_ARTIFACT_URL $ARTIFACTS_DIR/.
cd -
