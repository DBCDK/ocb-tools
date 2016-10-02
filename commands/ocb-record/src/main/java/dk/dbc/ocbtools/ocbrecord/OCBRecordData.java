package dk.dbc.ocbtools.ocbrecord;

import java.io.File;
import java.util.List;
import java.util.UUID;

class OCBRecordData {
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

    OCBRecordData() {
        this.uuid = UUID.randomUUID().toString();
    }

    File getBaseDir() {
        return baseDir;
    }

    void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    String getDistribution() {
        return distribution;
    }

    void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    String getOutputFile() {
        return outputFile;
    }

    void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    String getTemplate() {
        return template;
    }

    void setTemplate(String template) {
        this.template = template;
    }

    String getFormat() {
        return format;
    }

    void setFormat(String format) {
        this.format = format;
    }

    MarcType getFormatType() {
        return formatType;
    }

    void setFormatType(MarcType formatType) {
        this.formatType = formatType;
    }

    String getFaustNumber() {
        return faustNumber;
    }

    void setFaustNumber(String faustNumber) {
        this.faustNumber = faustNumber;
    }

    String getOpenNumberRollUrl() {
        return openNumberRollUrl;
    }

    void setOpenNumberRollUrl(String openNumberRollUrl) {
        this.openNumberRollUrl = openNumberRollUrl;
    }

    String getInputFile() {
        return inputFile;
    }

    void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    String getInputFileContentString() {
        return inputFileContentString;
    }

    void setInputFileContentString(String inputFileContentString) {
        this.inputFileContentString = inputFileContentString;
    }

    List<String> getInputFileContentList() {
        return inputFileContentList;
    }

    void setInputFileContentList(List<String> inputFileContentList) {
        this.inputFileContentList = inputFileContentList;
    }

    String getInputEncoding() {
        return inputEncoding;
    }

    void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    Boolean isRemote() {
        return remote;
    }

    void setRemote(Boolean remote) {
        this.remote = remote;
    }

    String getBuildWsUrl() {
        return buildWsUrl;
    }

    void setBuildWsUrl(String buildWsUrl) {
        this.buildWsUrl = buildWsUrl;
    }

    String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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
