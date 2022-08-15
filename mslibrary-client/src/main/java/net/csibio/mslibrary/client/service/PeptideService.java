package net.csibio.mslibrary.client.service;

import net.csibio.aird.bean.WindowRange;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.math.SlopeIntercept;
import net.csibio.mslibrary.client.domain.bean.peptide.PeptideCoord;
import net.csibio.mslibrary.client.domain.bean.peptide.Protein;
import net.csibio.mslibrary.client.domain.db.PeptideDO;
import net.csibio.mslibrary.client.domain.query.PeptideQuery;

import java.util.List;
import java.util.Map;


public interface PeptideService extends BaseService<PeptideDO, PeptideQuery> {

    List<PeptideDO> getAllByLibraryId(String libraryId);

    Result updateDecoyInfos(List<PeptideDO> peptides);

    Result removeAllByLibraryId(String libraryId);

    /**
     * 获取某一个标准库中所有的Transition的RT的取值范围
     *
     * @param libraryId
     * @return
     */
    Double[] getRTRange(String libraryId);

    /**
     * 计算不同蛋白质的数目
     *
     * @param libraryId
     * @return
     */
    Long countByProteinName(String libraryId);

    /**
     * 根据库构建用于进行irt计算的坐标数组
     *
     * @param libraryId
     * @param mzRange
     * @return
     */
    List<PeptideCoord> buildCoord4Irt(String libraryId, WindowRange mzRange);

    /**
     * 根据分析参数动态构建符合条件的目标肽段
     *
     * @param libraryId 指定库Id
     * @param mzRange   窗口范围
     * @param rtWindow  创建坐标的RT窗口
     * @param si        斜率截距
     * @return
     */
    List<PeptideCoord> buildCoord(String libraryId, WindowRange mzRange, Double rtWindow, SlopeIntercept si);

    /**
     * 根据PeptideRef生成一个全新的PeptideDO
     * 可以指定生成的a,b,c,x,y,z碎片类型 ionTypes
     * 可以指定生成的碎片的带电量种类 chargeTypes
     * 注意:生成的靶向肽段是没有预测rt和预测intensity的
     *
     * @param peptideRef
     * @return
     */
    PeptideDO buildWithPeptideRef(String peptideRef, int minLength, List<String> ionTypes, List<Integer> chargeTypes);


    /**
     * 构建蛋白质干扰图
     *
     * @param libraryId
     * @param proteinName
     * @param range
     * @param windowRanges
     * @return
     */
    Result<Map<String, List<Object>>> getPeptideLink(String libraryId, String proteinName, double range, List<WindowRange> windowRanges);


}

