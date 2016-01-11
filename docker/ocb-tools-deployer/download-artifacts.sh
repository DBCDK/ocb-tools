#!/bin/bash

ARTIFACTS_DIR=artifacts

OCB_TOOLS_ARTIFACT_URL=http://is.dbc.dk/job/ocb-tools-head-guesstimate/lastSuccessfulBuild/artifact/target/dist/ocb-tools-1.0.0.tgz
OPENCAT_BUSINESS_ARTIFACT_URL=http://is.dbc.dk/job/opencat-business-guesstimate-head/lastSuccessfulBuild/artifact/deploy/opencat-business.tgz

if [ -d "$ARTIFACTS_DIR" ]; then
    rm -rf $ARTIFACTS_DIR
fi

mkdir $ARTIFACTS_DIR

cd $ARTIFACTS_DIR
wget $OCB_TOOLS_ARTIFACT_URL $ARTIFACTS_DIR/.
wget $OPENCAT_BUSINESS_ARTIFACT_URL $ARTIFACTS_DIR/.
cd -
