/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.rawrepo.RecordId;

import java.math.BigDecimal;
import java.util.Map;

public class QueuedJob {

    private String bibliographicRecordId;
    private Integer agencyId;
    private String worker;

    private QueuedJob(String bibliographicRecordId, Integer agencyId, String worker) {
        this.bibliographicRecordId = bibliographicRecordId;
        this.agencyId = agencyId;
        this.worker = worker;
    }

    public static QueuedJob fromMap(Map<String, Object> map) {
        String bibliographicRecordId = (String) map.get("bibliographicrecordid");
        Integer agencyid = ((BigDecimal) map.get("agencyid")).intValue();
        String worker = (String) map.get("worker");

        return new QueuedJob(bibliographicRecordId, agencyid, worker);
    }

    public String getBibliographicRecordId() {
        return bibliographicRecordId;
    }

    public Integer getAgencyId() {
        return agencyId;
    }

    public String getWorker() {
        return worker;
    }

    public RecordId getRecordId() {
        return new RecordId(this.bibliographicRecordId, this.agencyId);
    }

    @Override
    public String toString() {
        return "QueuedJob{" +
                "bibliographicRecordId='" + bibliographicRecordId + '\'' +
                ", agencyId=" + agencyId +
                ", worker='" + worker + '\'' +
                '}';
    }
}
