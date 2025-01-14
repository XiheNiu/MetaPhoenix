package net.csibio.metaphoenix.client.domain.db;

import lombok.Data;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.metaphoenix.client.constants.enums.IonMode;
import net.csibio.metaphoenix.client.domain.bean.spectrum.AnnotationHistory;
import net.csibio.metaphoenix.client.utils.CompressUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "spectrum")
public class SpectrumDO {

    /**
     * Common Item
     */

    @Id
    String id;

    boolean isDecoy = false;

    @Indexed
    String rawSpectrumId;

    @Indexed
    String compoundId;

    @Indexed
    String libraryId;

    //The outer database id
    @Indexed
    String spectrumId;

    @Indexed
    Integer msLevel;

    String splash;

    @Indexed
    String ionSource;

    String inChIKey;

    String compoundName;

    String instrumentType;

    @Indexed
    String instrument;

    @Indexed
    String precursorAdduct;

    @Indexed
    Double precursorMz;

    /**
     * @see IonMode
     */
    @Indexed
    String ionMode;

    @Indexed
    Double collisionEnergy;

    String formula;

    Double exactMass;

    String comment;

    byte[] byteMzs;
    byte[] byteInts;

    @Transient
    double[] mzs;
    @Transient
    double[] ints;

    Date createDate;

    Date updateDate;

    /**
     * GNPS Item
     */
    String ontology;

    String sourceFile;

    String task;

    Integer scan;

    String libraryMembership;

    @Indexed
    Integer spectrumStatus;

    String submitUser;

    String compoundSource;

    String pi;

    String dataCollector;

    Integer charge;

    String casNumber;

    String pubmedId;

    String smiles;

    String inChI;

    String inchiAUX;

    Integer libraryClass;

    String taskId;

    String userId;

    String inchiKeySmiles;

    String inchiKeyInchi;

    String formulaSmiles;

    String formulaInchi;

    List<AnnotationHistory> annotationHistoryList;

    String url;

    /**
     * HMDB Item
     */
    String notes;

    String sampleSource;

    boolean predicted;

    String structureId;

    /**
     * MassBank Item
     */
    String synon;

    public double[] getMzs() {
        if (mzs == null && byteMzs != null) {
            mzs = CompressUtil.decode(byteMzs);
        }
        return mzs;
    }

    public void setMzs(double[] mzs) {
        this.mzs = mzs;
        this.byteMzs = CompressUtil.encode(mzs);
    }

    public void decode() {
        this.ints = CompressUtil.decode(byteInts);
        this.mzs = CompressUtil.decode(byteMzs);
        this.byteInts = null;
        this.byteMzs = null;
    }

    public void encode() {
        this.byteMzs = CompressUtil.encode(mzs);
        this.byteInts = CompressUtil.encode(ints);
    }

    public double[] getInts() {
        if (ints == null && byteInts != null) {
            ints = CompressUtil.decode(byteInts);
        }
        return ints;
    }

    public void setInts(double[] ints) {
        this.ints = ints;
        this.byteInts = CompressUtil.encode(ints);
    }

    public Spectrum getSpectrum() {
        return new Spectrum(getMzs(), getInts());
    }

}
