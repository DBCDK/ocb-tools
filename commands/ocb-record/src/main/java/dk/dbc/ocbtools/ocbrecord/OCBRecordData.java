package dk.dbc.ocbtools.ocbrecord;

import java.io.File;
import java.util.List;
import java.util.UUID;

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
    private String openNumberRollUrl = null;
    private String inputFile = null;
    private String inputFileContentString = null;
    private List<String> inputFileContentList = null;
    private String inputEncoding = null;
    private Boolean remote = false;
    private String buildWsUrl = null;
    private String uuid = null;

    public OCBRecordData() {
        this.uuid = UUID.randomUUID().toString();
    }

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

    public Boolean isRemote() {
        return remote;
    }

    public void setRemote( Boolean remote ) {
        this.remote = remote;
    }

    public String getBuildWsUrl() {
        return buildWsUrl;
    }

    public void setBuildWsUrl( String buildWsUrl ) {
        this.buildWsUrl = buildWsUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid( String uuid ) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "OCBRecordData{" +
                "baseDir=" + baseDir +
                ", distribution='" + distribution + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", template='" + template + '\'' +
                ", format='" + format + '\'' +
                ", formatType=" + formatType +
                ", faustNumber='" + faustNumber + '\'' +
                ", openNumberRollUrl='" + openNumberRollUrl + '\'' +
                ", inputFile='" + inputFile + '\'' +
                ", inputFileContentString='" + inputFileContentString + '\'' +
                ", inputFileContentList=" + inputFileContentList +
                ", inputEncoding='" + inputEncoding + '\'' +
                ", remote=" + remote +
                ", buildWsUrl='" + buildWsUrl + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
