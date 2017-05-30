/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.ocbtools.testengine.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class QueueSetup {

    private List<String> inserts;


    QueueSetup() {
    }

    List<String> getQueueRulesInserts() throws IOException {
        if (inserts == null) {
            List<String> result = new ArrayList<>();

            InputStream stream = getClass().getResourceAsStream("/queuerules.sql");
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = r.readLine()) != null) {
                result.add(line);
            }

            inserts = result;
        }

        return inserts;
    }

}
