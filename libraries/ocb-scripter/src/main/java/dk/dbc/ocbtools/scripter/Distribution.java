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

    private String schemaName;
    private String dirName;
}
