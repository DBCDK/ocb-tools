package dk.dbc.ocbtools.ocbrecord;

import java.io.File;
import java.util.List;

/**
 * Created by thl on 3/3/15.
 */
public class OCBRecordData {
    private File baseDir = null;
    private String distribution = null;
    private String outputFile = null;
    private String template = null;
    private String format = null;
    private MarcType formatType = null;
    private String faustNumber = null;
    private String buildUrl = null;
    private String openNumberRollUrl = null;
    private String inputFile = null;
    private String inputFileContentString = null;
    private List<String> inputFileContentList = null;
    private String inputEncoding = null;

    public OCBRecordData() {}

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir( File baseDir ) {
        this.baseDir = baseDir;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution( String distribution ) {
        this.distribution = distribution;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile( String outputFile ) {
        this.outputFile = outputFile;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate( String template ) {
        this.template = template;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat( String format ) {
        this.format = format;
    }

    public MarcType getFormatType() {
        return formatType;
    }

    public void setFormatType( MarcType formatType ) {
        this.formatType = formatType;
    }

    public String getFaustNumber() {
        return faustNumber;
    }

    public void setFaustNumber( String faustNumber ) {
        this.faustNumber = faustNumber;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public void setBuildUrl( String buildUrl ) {
        this.buildUrl = buildUrl;
    }

    public String getOpenNumberRollUrl() {
        return openNumberRollUrl;
    }

    public void setOpenNumberRollUrl( String openNumberRollUrl ) {
        this.openNumberRollUrl = openNumberRollUrl;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile( String inputFile ) {
        this.inputFile = inputFile;
    }

    public String getInputFileContentString() {
        return inputFileContentString;
    }

    public void setInputFileContentString( String inputFileContentString ) {
        this.inputFileContentString = inputFileContentString;
    }

    public List<String> getInputFileContentList() {
        return inputFileContentList;
    }

    public void setInputFileContentList( List<String> inputFileContentList ) {
        this.inputFileContentList = inputFileContentList;
    }

    public String getInputEncoding() {
        return inputEncoding;
    }

    public void setInputEncoding( String inputEncoding ) {
        this.inputEncoding = inputEncoding;
    }

    @Override
    public String toString() {
        return "OCBRecordData{" +
                "baseDir=" + baseDir +
                ", distribution='" + distribution + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", template='" + template + '\'' +
                ", format='" + format + '\'' +
                ", faustNumber='" + faustNumber + '\'' +
                ", buildUrl='" + buildUrl + '\'' +
                ", openNumberRollUrl='" + openNumberRollUrl + '\'' +
                '}';
    }
}
