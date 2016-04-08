//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbrequests.rawrepo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

//-----------------------------------------------------------------------------
public class RecordEntityTest {
    @BeforeClass
    public static void connectDatabase() {
        emf = Persistence.createEntityManagerFactory( "ocb-requests" );
    }

    @AfterClass
    public static void disconnectDatabase() {
        if( emf != null ) {
            emf.close();
        }
    }

    @Test
    public void testFindRecords() {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createNamedQuery( "countRecordsByAgencyId" );
            query.setParameter( "agencyid", 850160 );

            assertThat( query.getSingleResult(), equalTo( 57466L ) );
        }
        finally {
            em.close();
        }
    }

    private static EntityManagerFactory emf;
}
