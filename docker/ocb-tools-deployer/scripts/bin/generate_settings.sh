1#!/bin/bash

set -e

mkdir -p ${OCB_USER_HOME}/.ocb-tools
cat << EOF > ${OCB_USER_HOME}/.ocb-tools/settings.properties
updateservice.dataio.url = http://${UPDATE_DATAIO_PORT_8080_TCP_ADDR}:${UPDATE_DATAIO_PORT_8080_TCP_PORT}
updateservice.fbs.url = http://${UPDATE_FBS_PORT_8080_TCP_ADDR}:${UPDATE_FBS_PORT_8080_TCP_PORT}

rawrepo.jdbc.driver = org.postgresql.Driver
rawrepo.jdbc.conn.url = jdbc:postgresql://${RAWREPO_PORT_5432_TCP_ADDR}:${RAWREPO_PORT_5432_TCP_PORT}/${RAWREPO_DBNAME}
rawrepo.jdbc.conn.user = ${RAWREPO_USER}
rawrepo.jdbc.conn.passwd = ${RAWREPO_PASSWORD}

holdings.jdbc.driver = org.postgresql.Driver
holdings.jdbc.conn.url = jdbc:postgresql://${HOLDINGSITEMS_PORT_5432_TCP_ADDR}:${HOLDINGSITEMS_PORT_5432_TCP_PORT}/${HOLDINGS_ITEMS_DBNAME}
holdings.jdbc.conn.user = ${HOLDINGS_ITEMS_USER}
holdings.jdbc.conn.passwd = ${HOLDINGS_ITEMS_PASSWORD}

solr.port = 8080

request.headers.x.forwarded.for = ${REQUEST_IP_ADDR}

rawrepo.provider.name = opencataloging-update
EOF
