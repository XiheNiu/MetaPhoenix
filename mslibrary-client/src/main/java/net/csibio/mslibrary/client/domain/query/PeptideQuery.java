package net.csibio.mslibrary.client.domain.query;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PeptideQuery extends PageQuery {

    String id;

    String libraryId;

    /**
     * 是否是该蛋白对应的具有唯一性的肽段
     */
    Boolean isUnique;

    /**
     * query默认不查询disable为true的肽段
     */
    Boolean disable = false;

    /**
     * 对应肽段序列
     */
    String sequence;

    /**
     * 对应蛋白质名称
     */
    String protein;

    /**
     * 肽段
     */
    String peptideRef;

    /**
     * 完整版肽段名称(含修饰基团)
     */
    String fullName;

    Double mzStart;

    Double mzEnd;

    Double rtStart;

    Double rtEnd;

    Boolean estimateCount = true;

    public PeptideQuery() {
    }

    public PeptideQuery(String libraryId) {
        this.libraryId = libraryId;
    }

    public PeptideQuery(String libraryId, String peptideRef) {
        this.libraryId = libraryId;
        this.peptideRef = peptideRef;
    }

}
