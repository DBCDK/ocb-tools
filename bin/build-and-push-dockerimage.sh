#!/bin/bash
function die() {
  echo "Error:" "$@"
  exit 1
}

function sanityCheckInput(){
  if [ ${USER} != "isworker" ]; then
    die "This program is only meant to run on an Jenkins node"
  fi
}

function tagAndPushToArty () {
  TAG_LATEST="docker-i.dbc.dk/ocb-tools-deployer:latest"
  docker build --no-cache -t ${TAG_LATEST} -f docker/ocb-tools-deployer/Dockerfile docker/ocb-tools-deployer/\
  --label=svn="${SVN_REVISION}"\
  --label=user="${USER}"\
  --label=jobname="${JOB_NAME}"\
  --label=buildnumber="${BUILD_NUMBER}"\ || die "Building "${TAG_LATEST}" failed"
  docker push "$TAG_LATEST"				|| die "failed to push $TAG_LATEST"
  docker rmi "$TAG_LATEST"				|| die "failed to remove image $TAG_LATEST"
}


set -e
sanityCheckInput
tagAndPushToArty