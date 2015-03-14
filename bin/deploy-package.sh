#!/bin/bash

cd ../target/dist

mkdir -p ocb-tools-1.0.0/doc
cp -r ../site/* ocb-tools-1.0.0/doc

tar -c -z ocb-tools-1.0.0 > ocb-tools-1.0.0.tgz
