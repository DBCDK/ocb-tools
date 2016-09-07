//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbrequests.rawrepo;

//-----------------------------------------------------------------------------
import javax.persistence.*;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import static javax.persistence.FetchType.LAZY;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 04/04/16.
 */
@Entity
@Table( name = "records", schema = "public" )
@IdClass( RecordEntityKey.class )
@NamedQueries( {
    @NamedQuery(
        name = "countRecordsByAgencyId",
        query = "select count(r) from RecordEntity r where r.agencyId = :agencyid"
    ),
    @NamedQuery(
        name = "findRecordsByAgencyId",
        query = "select r from RecordEntity r where r.agencyId = :agencyid"
    )
}
)
public class RecordEntity {


    @Id
    @Column( name = "bibliographicrecordid", length = 64)
    private String bibliographicRecordId;

    @Id
    @Column( name = "agencyid" )
    private Integer agencyId;

    @Column( name = "deleted" )
    private Boolean deleted;

    @Column( name = "mimetype", length = 128 )
    private String mimeType;

    @Lob
    @Column( name = "content" )
    private String content;

    @Column( name = "created" )
    private Date created;

    @Column( name = "modified" )
    private Date modified;

    @Column( name = "trackingid", length = 255 )
    private String trackingId;

    public String getBibliographicRecordId() {
        return bibliographicRecordId;
    }

    public void setBibliographicRecordId( String bibliographicRecordId ) {
        this.bibliographicRecordId = bibliographicRecordId;
    }

    public Integer getAgencyId() {
        return agencyId;
    }

    public void setAgencyId( Integer agencyId ) {
        this.agencyId = agencyId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted( Boolean deleted ) {
        this.deleted = deleted;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType( String mimeType ) {
        this.mimeType = mimeType;
    }

    public String getContent() {
        return content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated( Date created ) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified( Date modified ) {
        this.modified = modified;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId( String trackingId ) {
        this.trackingId = trackingId;
    }

    public String contentAsXml() throws UnsupportedEncodingException {
        return new String( DatatypeConverter.parseBase64Binary( content ), "UTF-8" );
    }

}
