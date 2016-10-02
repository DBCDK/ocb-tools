package dk.dbc.ocbtools.commons.filesystem;


import java.io.File;
import java.io.FilenameFilter;

class FileExtensionFilter implements FilenameFilter {

    private String ext;

    FileExtensionFilter(String ext) {
        this.ext = ext;
    }

    @Override
    public boolean accept( File dir, String name ) {
        return name.endsWith( ext );
    }

}
