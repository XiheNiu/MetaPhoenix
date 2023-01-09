package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.domain.bean.spectrum.AnnotationHistory;
import net.csibio.mslibrary.client.domain.bean.spectrum.IonPeak;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import net.csibio.mslibrary.client.utils.CompressUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
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

    String compoundName;

    String instrumentType;

    @Indexed
    String instrument;

    @Indexed
    String precursorAdduct;

    @Indexed
    Double precursorMz;

    /**
     * @see net.csibio.mslibrary.client.constants.enums.IonMode
     */
    @Indexed
    String ionMode;

    @Indexed
    Double collisionEnergy;

    String formula;

    Double exactMass;

    String comments;

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

    String inChIKey;

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

    public List<IonPeak> getIonPeaks() {
        List<IonPeak> ionPeaks = new ArrayList<>();
        for (int i = 0; i < getMzs().length; i++) {
            IonPeak ionPeak = new IonPeak(getMzs()[i], getInts()[i]);
            ionPeaks.add(ionPeak);
        }
        return ionPeaks;
    }

    public List<IonPeak> getIonPeaksWithoutPrecursor() {
        List<IonPeak> ionPeaks = new ArrayList<>();
        int index = ArrayUtil.binarySearch(getMzs(), getPrecursorMz());
        for (int i = 0; i < getMzs().length; i++) {
            if (i == index) {
                continue;
            }
            IonPeak ionPeak = new IonPeak(getMzs()[i], getInts()[i]);
            ionPeaks.add(ionPeak);
        }
        return ionPeaks;
    }

}
