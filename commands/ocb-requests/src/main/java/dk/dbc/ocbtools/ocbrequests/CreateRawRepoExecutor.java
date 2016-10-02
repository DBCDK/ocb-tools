package dk.dbc.ocbtools.ocbrequests;

import dk.dbc.iscrum.records.AgencyNumber;
import dk.dbc.iscrum.records.MarcConverter;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcRecordReader;
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.ocbrequests.rawrepo.RecordEntity;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Executor for the subcommand 'rawrepo-create'
 */
class CreateRawRepoExecutor implements SubcommandExecutor {

    private static final XLogger logger = XLoggerFactory.getXLogger(CreateRawRepoExecutor.class);
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);

    private File baseDir;
    private Integer agencyId;
    private Integer userCount;
    private Integer requestsPeerUser;

    CreateRawRepoExecutor() {
    }

    void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    void setAgencyId(Integer agencyId) {
        this.agencyId = agencyId;
    }

    void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    void setRequestsPeerUser(Integer requestsPeerUser) {
        this.requestsPeerUser = requestsPeerUser;
    }

    /**
     * Performes the actions for the subcommand.
     */
    @Override
    public void actionPerformed() throws CliException {
        logger.entry();
        try {
            if (baseDir == null) {
                baseDir = new File(".").getCanonicalFile();
            }

            output.info("Base dir: {}", baseDir);
            output.info("Agency id: {}", agencyId);
            output.info("Number of users: {}", userCount);
            output.info("Requests peer user: {}", requestsPeerUser);
            output.info("");

            ExecutorService pool = Executors.newCachedThreadPool();

            EntityManagerFactory emf = Persistence.createEntityManagerFactory("ocb-requests");

            EntityManager em = emf.createEntityManager();

            int userNo = 1;
            int requestNo = 1;

            boolean done = false;
            int queryNextResultsIndex = 0;
            final int queryMaxResults = 100;
            while (!done) {
                TypedQuery<RecordEntity> query = em.createNamedQuery("findRecordsByAgencyId", RecordEntity.class);
                query.setParameter("agencyid", agencyId);
                query.setFirstResult(queryNextResultsIndex);
                query.setMaxResults(queryMaxResults);

                List<RecordEntity> records = query.getResultList();

                for (RecordEntity record : records) {
                    MarcRecord recordData = MarcConverter.convertFromMarcXChange(record.contentAsXml());
                    MarcRecordReader reader = new MarcRecordReader(recordData);

                    if (reader.markedForDeletion()) {
                        continue;
                    }
                    if (!reader.hasValue("004", "a", "e")) {
                        continue;
                    }

                    if (requestNo == 1) {
                        output.info("Creating requests for User: {}", userNo);
                    }

                    CreateRequestTask task = new CreateRequestTask(baseDir);
                    task.setAgencyId(new AgencyNumber(agencyId));
                    task.setUserNumber(userNo);
                    task.setRequestNumber(requestNo);
                    task.setRecord(record);

                    pool.submit(task);
                    //output.info( "Created task for User: {}-{}", userNo, requestNo );

                    requestNo++;
                    if (requestNo > requestsPeerUser) {
                        userNo++;
                        requestNo = 1;
                    }

                    if (userNo > userCount) {
                        done = true;
                        break;
                    }
                }

                queryNextResultsIndex += queryMaxResults;
            }

            pool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(380, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(380, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }
}
