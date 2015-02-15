//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.filesystem;

//-----------------------------------------------------------------------------
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 14/02/15.
 */
public class FileIgnoreFilter implements FilenameFilter {
    public FileIgnoreFilter( List<String> ignoreNames ) {
        this.ignoreNames = ignoreNames;
    }

    public FileIgnoreFilter( String... ignoreNames ) {
        this( Arrays.asList( ignoreNames ) );
    }

    @Override
    public boolean accept( File dir, String name ) {
        return !ignoreNames.contains( name );
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private List<String> ignoreNames;
}
