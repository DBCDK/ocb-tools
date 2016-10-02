package dk.dbc.ocbtools.commons.filesystem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

class FileIgnoreFilter implements FilenameFilter {
    private List<String> ignoreNames;

    private FileIgnoreFilter(List<String> ignoreNames) {
        this.ignoreNames = ignoreNames;
    }

    FileIgnoreFilter(String... ignoreNames) {
        this(Arrays.asList(ignoreNames));
    }

    @Override
    public boolean accept(File dir, String name) {
        return !ignoreNames.contains(name);
    }

}
