-- 
-- dbc-rawrepo-access
-- Copyright (C) 2015 Dansk Bibliotekscenter a/s, Tempovej 7-11, DK-2750 Ballerup,
-- Denmark. CVR: 15149043
--
-- This file is part of dbc-rawrepo-access.
--
-- dbc-rawrepo-access is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- dbc-rawrepo-access is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with dbc-rawrepo-access. If not, see <http://www.gnu.org/licenses/>.
-- 

--
-- broend-sync wants all searchable records affected by modification
--
INSERT INTO queueworkers (worker) VALUES ('basis-decentral');
INSERT INTO queueworkers (worker) VALUES ('broend-sync');
INSERT INTO queueworkers (worker) VALUES ('broend30-ph-sync');
INSERT INTO queueworkers (worker) VALUES ('broend30-sync');
INSERT INTO queueworkers (worker) VALUES ('danbib-libv3');
INSERT INTO queueworkers (worker) VALUES ('danbib-ph-libv3');
INSERT INTO queueworkers (worker) VALUES ('dataio-bulk-sync');
INSERT INTO queueworkers (worker) VALUES ('dataio-socl-sync-bulk');
INSERT INTO queueworkers (worker) VALUES ('ims-bulk-sync');
INSERT INTO queueworkers (worker) VALUES ('ims-sync');
INSERT INTO queueworkers (worker) VALUES ('oai-set-matcher');
INSERT INTO queueworkers (worker) VALUES ('socl-sync');
INSERT INTO queueworkers (worker) VALUES ('socl-sync-prio1');
INSERT INTO queueworkers (worker) VALUES ('solr-a-sync');
INSERT INTO queueworkers (worker) VALUES ('solr-b-sync');
INSERT INTO queueworkers (worker) VALUES ('solr-sync-bulk');


INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency-delete','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency-delete','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency-maintain','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency-maintain','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency30-delete','broend30-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency3x-delete','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency3x-delete','broend30-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('agency3x-delete','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('bulk-broend','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-bulk','dataio-bulk-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-bulk','dataio-socl-sync-bulk','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-bulk','oai-set-matcher','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-ph-holding-update','broend30-ph-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-ph-holding-update','danbib-ph-libv3','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update','oai-set-matcher','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-all_display_bases','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-all_display_bases','broend30-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-all_display_bases','danbib-libv3','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-all_display_bases','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-danbib','danbib-libv3','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-danbib','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-well3.0','broend30-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-well3.5','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-well3.5','ims-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-well3.5','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-well3.x','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-well3.x','broend30-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('dataio-update-well3.x','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-ph-update','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-ph-update','broend30-ph-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-ph-update','danbib-ph-libv3','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-ph-update','socl-sync-prio1','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-update','basis-decentral','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-update','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-update','ims-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('fbs-update','socl-sync-prio1','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('ims','ims-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('ims-bulk','ims-bulk-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('opencataloging-update','basis-decentral','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('opencataloging-update','broend-sync','A','Y');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('opencataloging-update','socl-sync','Y','A');
INSERT INTO queuerules (provider, worker, changed, leaf) VALUES ('update-rawrepo-solr-sync','socl-sync','Y','N');