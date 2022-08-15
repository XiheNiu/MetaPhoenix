package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.bean.peptide.FragmentInfo;
import net.csibio.mslibrary.client.domain.bean.peptide.PeptideCoord;
import net.csibio.mslibrary.client.utils.CompressUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Document(collection = "peptide")
@CompoundIndexes({
        @CompoundIndex(name = "libraryId_peptideRef", def = "{'libraryId':1,'peptideRef':1}", unique = true),
        @CompoundIndex(name = "libraryId_proteins", def = "{'libraryId':1,'proteins':1}")
})
public class PeptideDO {

    @Id
    String id;

    /**
     * 对应的标准库ID
     */
    @Indexed
    String libraryId;

    /**
     * 肽段的唯一识别符,格式为 : fullName_precursorCharge,如果是伪肽段,则本字段为对应的真肽段的PeptideRef
     */
    @Indexed
    String peptideRef;

    /**
     * 肽段的荷质比MZ
     */
    @Indexed
    Double mz;

    /**
     * 蛋白质标识符
     */
    @Indexed
    Set<String> proteins;

    /**
     * 该肽段是否是该蛋白的不重复肽段
     */
    Boolean isUnique = true;

    /**
     * 是否禁用
     */
    Boolean disable = false;

    /**
     * 肽段的归一化RT
     */
    Double rt;

    /**
     * 去除了修饰基团的肽段序列
     */
    String sequence;

    /**
     * 包含了修饰基团的肽段序列
     */
    String fullName;

    /**
     * 肽段带电量
     */
    Integer charge;

    /**
     * key为unimod在肽段中的位置,位置从0开始计数,value为unimod的Id(参见unimod.obo文件)
     */
    HashMap<Integer, String> unimodMap;

    /**
     * 对应的肽段碎片的信息
     * key为cutinfo
     * 默认按照intensity从大到小排列
     */
    List<FragmentInfo> fragments = new ArrayList<>();

    /**
     * 伪肽段的信息
     */
    String decoySequence;

    HashMap<Integer, String> decoyUnimodMap;

    List<FragmentInfo> decoyFragments = new ArrayList<>();

    /**
     * 扩展字段
     */
    String features;

    byte[] fingerPrintsByte;

    //b:y
    String fingerFeature;

    @Transient
    Set<Float> fingerPrints; //肽段指纹

    public void clearDecoy() {
        this.decoyFragments = new ArrayList<>();
        this.decoyUnimodMap = new HashMap<>();
        this.decoySequence = null;
    }

    public void setProteins(Set<String> proteins) {
        this.proteins = proteins;
        this.isUnique = (proteins != null && proteins.size() == 1);
    }

    public PeptideCoord toTargetPeptide() {
        PeptideCoord tp = new PeptideCoord();
        tp.setProteins(new ArrayList<>(proteins));
        tp.setPeptideRef(peptideRef);
        tp.setRt(rt);
        tp.setFragments(fragments);
        tp.setMz(mz);
        tp.setCharge(charge);
        tp.setSequence(sequence);
        tp.setUnimodMap(unimodMap);
        tp.setDecoySequence(decoySequence);
        tp.setDecoyUnimodMap(decoyUnimodMap);
        tp.setDecoyFragments(decoyFragments);
        return tp;
    }

    /**
     * 根据新的带电量生成新的肽段PeptideRef
     *
     * @param newCharge
     * @return
     */
    public PeptideDO buildBrother(int newCharge) {
        PeptideDO peptide = new PeptideDO();
        peptide.setIsUnique(isUnique);
        peptide.setUnimodMap(unimodMap);
        peptide.setFullName(fullName);
        peptide.setCharge(newCharge);
        peptide.setProteins(proteins);
        peptide.setSequence(sequence);
        peptide.setRt(rt);
        peptide.setCharge(charge);
        peptide.setPeptideRef(fullName + SymbolConst.UNDERLINE + newCharge);
        peptide.setMz(mz * charge / newCharge);
        return peptide;
    }
}
