//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.filesystem;

//-----------------------------------------------------------------------------

import java.io.File;
import java.io.FilenameFilter;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 14/02/15.
 */
public class FileExtensionFilter implements FilenameFilter {
    public FileExtensionFilter( String ext ) {
        this.ext = ext;
    }

    @Override
    public boolean accept( File dir, String name ) {
        return name.endsWith( ext );
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private String ext;
}
