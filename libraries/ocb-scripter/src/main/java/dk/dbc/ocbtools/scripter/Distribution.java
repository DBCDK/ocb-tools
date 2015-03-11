//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.scripter;

//-----------------------------------------------------------------------------
public class Distribution {
    public Distribution( String schemaName, String dirName ) {
        this.schemaName = schemaName;
        this.dirName = dirName;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public String getDirName() {
        return this.dirName;
    }

    @Override
    public String toString() {
        return String.format( "{schemaName: %s, dirName: %s}", schemaName, dirName );
    }

    private String schemaName;
    private String dirName;
}
