#!/bin/bash

generate_settings.sh

whoami
env

cd ${HOME}/opencat-business

ocb-test.sh run --application Update --summary --remote --config settings
cd -

mkdir -p results

cp opencat-business/*.log results/.
