package net.csibio.mslibrary.core.export;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.algorithm.similarity.Entropy;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("reporter")
@Slf4j
public class Reporter {

    @Autowired
    VMProperties vmProperties;
    @Autowired
    SpectrumService spectrumService;

    public void scoreGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export score graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "TargetFrequency", "DecoyFrequency", "TotalFrequency", "CTDC_FDR",
                "BestSTDS_FDR", "STDS_FDR", "true_FDR", "standard_FDR", "pValue", "PIT", "true_Count", "false_Count");
        List<List<Object>> dataSheet = getDataSheet(hitsMap, scoreInterval);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export score graph success : " + outputFileName);
    }

    private List<List<Object>> getDataSheet(ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval) {
        List<List<Object>> dataSheet = new ArrayList<>();

        //all hits above a score threshold for the target-decoy strategy
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();

        //the top score hits of each spectrum
        List<LibraryHit> bestDecoyHits = new ArrayList<>();
        List<LibraryHit> bestTargetHits = new ArrayList<>();
        List<LibraryHit> ctdcList = new ArrayList<>();

        //for true FDR calculation
        List<LibraryHit> trueHits = new ArrayList<>();
        List<LibraryHit> falseHits = new ArrayList<>();

        //collect data
        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                //concatenated target-decoy competition
                v.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                ctdcList.add(v.get(0));

                //separated target-decoy competition
                Map<Boolean, List<LibraryHit>> decoyTargetMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                List<LibraryHit> targetHitsList = decoyTargetMap.get(false);
                List<LibraryHit> decoyHitsList = decoyTargetMap.get(true);
                if (targetHitsList.size() != 0) {
                    targetHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    bestTargetHits.add(targetHitsList.get(0));
                    targetHits.addAll(targetHitsList);
                    for (LibraryHit hit : targetHitsList) {
                        if (hit.getSmiles().equals(k.getSmiles())) {
                            trueHits.add(hit);
                        } else {
                            falseHits.add(hit);
                        }
                    }
                }
                if (decoyHitsList != null && decoyHitsList.size() != 0) {
                    decoyHitsList.sort(Comparator.comparing(LibraryHit::getScore).reversed());
                    bestDecoyHits.add(decoyHitsList.get(0));
                    decoyHits.addAll(decoyHitsList);
                }
            }
        });

        //score range and step
        double minScore = 0.0;
        double maxScore = 1.0;
        double step = (maxScore - minScore) / scoreInterval;

        for (int i = 0; i < scoreInterval; i++) {
            double finalMinScore = minScore + i * step;
            double finalMaxScore = minScore + (i + 1) * step;
            int targetCount, decoyCount, rightCount, falseCount, bestTargetCount, bestDecoyCount;
            List<Object> row = new ArrayList<>();

            //concatenated target-decoy strategy calculation
            double CTDC_FDR = (double) 2 * ctdcList.stream().filter(hit -> hit.getScore() > finalMinScore && hit.isDecoy()).toList().size()
                    / ctdcList.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();

            //separated target-decoy strategy calculation
            targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            bestTargetCount = bestTargetHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            bestDecoyCount = bestDecoyHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            double PIT = (double) (targetCount - bestTargetCount) / targetCount;
            double BestSTDS_FDR = (double) bestDecoyCount / (bestTargetCount + bestDecoyCount) * PIT;
            double STDS_FDR = (double) decoyCount / targetCount;
            double pValue = (double) decoyCount / (targetCount);

            //real data calculation
            rightCount = trueHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            falseCount = falseHits.stream().filter(hit -> hit.getScore() > finalMinScore).toList().size();
            double trueFDR = (double) falseCount / (rightCount + falseCount);

            //hits distribution
            if (i == 0) {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            } else {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            }

            //write data sheet
            //start score
            row.add(finalMinScore);
            //end score
            row.add(finalMaxScore);
            //target frequency
            row.add((double) targetCount / targetHits.size());
            //decoy frequency
            row.add((double) decoyCount / decoyHits.size());
            //total frequency
            row.add((double) (targetCount + decoyCount) / (targetHits.size() + decoyHits.size()));
            //CTDC FDR
            row.add(CTDC_FDR);
            //BestSTDS FDR
            row.add(BestSTDS_FDR);
            //STDS FDR
            row.add(STDS_FDR);
            //trueFDR
            row.add(trueFDR);
            //standard FDR
            row.add(1 - finalMinScore);
            //pValue
            row.add(pValue);
            //PIT
            row.add(PIT);
            //true count
            row.add(rightCount);
            //false count
            row.add(falseCount);
            dataSheet.add(row);
        }
        return dataSheet;
    }

    public void estimatedPValueGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int pInterval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export estimatedPValue graph : " + outputFileName);
        List<List<Object>> scoreDataSheet = getDataSheet(hitsMap, 100 * pInterval);

        //reverse score data sheet to make pValue ascending
        Collections.reverse(scoreDataSheet);
        List<List<Object>> dataSheet = new ArrayList<>();
        List<Double> thresholds = new ArrayList<>();

        //pValue thresholds
        double step = 1.0 / pInterval;
        for (int i = 0; i < pInterval; i++) {
            thresholds.add(step * (i + 1));
        }

        //record real pValue and sum frequencies from pValue 0~1
        double[] pValueArray = new double[scoreDataSheet.size()];
        double targetFrequency = 0.0;
        double decoyFrequency = 0.0;
        double totalFrequency = 0.0;
        List<Double> targetFrequencyList = new ArrayList<>();
        List<Double> decoyFrequencyList = new ArrayList<>();
        List<Double> totalFrequencyList = new ArrayList<>();
        for (int i = 0; i < scoreDataSheet.size(); i++) {
            pValueArray[i] = (double) scoreDataSheet.get(i).get(6);
            targetFrequency += (double) scoreDataSheet.get(i).get(2);
            decoyFrequency += (double) scoreDataSheet.get(i).get(3);
            totalFrequency += (double) scoreDataSheet.get(i).get(4);
            targetFrequencyList.add(targetFrequency);
            decoyFrequencyList.add(decoyFrequency);
            totalFrequencyList.add(totalFrequency);
        }

        //for each threshold, find the nearest pValue and record the index
        List<Integer> indexList = new ArrayList<>();
        for (double threshold : thresholds) {
            int index = ArrayUtil.findNearestIndex(pValueArray, threshold);
            double diff = Math.abs(pValueArray[index] - threshold);
            if (diff > step) {
                indexList.add(-1);
            } else {
                indexList.add(index);
            }
        }

        //header
        List<Object> header = Arrays.asList("EstimatedPValue", "RealPValue", "TargetFrequency", "DecoyFrequency", "TotalFrequency");
        dataSheet.add(header);
        //calculate the frequency of each threshold according to the index
        for (int i = 0; i < indexList.size(); i++) {
            int index = indexList.get(i);
            List<Object> row = new ArrayList<>();
            row.add(thresholds.get(i));
            if (index == -1) {
                row.add("NA");
                row.add("NA");
                row.add("NA");
                row.add("NA");
            } else {
                row.add(scoreDataSheet.get(index).get(6));
                if (i == 0) {
                    row.add(targetFrequencyList.get(index));
                    row.add(decoyFrequencyList.get(index));
                    row.add(totalFrequencyList.get(index));
                } else {
                    if (indexList.get(i - 1) == -1) {
                        row.add(targetFrequencyList.get(index));
                        row.add(decoyFrequencyList.get(index));
                        row.add(totalFrequencyList.get(index));
                    } else {
                        row.add(targetFrequencyList.get(index) - targetFrequencyList.get(indexList.get(i - 1)));
                        row.add(decoyFrequencyList.get(index) - decoyFrequencyList.get(indexList.get(i - 1)));
                        row.add(totalFrequencyList.get(index) - totalFrequencyList.get(indexList.get(i - 1)));
                    }
                }
            }
            dataSheet.add(row);
        }

        EasyExcel.write(outputFileName).sheet("estimatedPValueGraph").doWrite(dataSheet);
        log.info("export estimatedPValue graph success: " + outputFileName);
    }

    public void entropyDistributionGraph(String fileName, String libraryId, int interval) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export entropy distribution graph : " + outputFileName);
        List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
        List<List<Object>> dataSheet = new ArrayList<>();
        if (spectrumDOS.size() != 0) {
            List<Double> entropyList = new ArrayList<>();
            for (SpectrumDO spectrumDO : spectrumDOS) {
                entropyList.add(Entropy.getEntropy(spectrumDO.getSpectrum()));
            }
            Collections.sort(entropyList);
            double minValue = entropyList.get(0);
            double maxValue = entropyList.get(entropyList.size() - 1);
            double step = (maxValue - minValue) / interval;
            for (int i = 0; i < interval; i++) {
                List<Object> data = new ArrayList<>();
                double minThreshold = minValue + i * step;
                double maxThreshold = minValue + (i + 1) * step;
                double fraction = (double) entropyList.stream().filter(entropy -> entropy > minThreshold && entropy <= maxThreshold).toList().size() / entropyList.size();
                data.add(minThreshold);
                data.add(maxThreshold);
                data.add(fraction);
                dataSheet.add(data);
            }
            EasyExcel.write(outputFileName).sheet("entropyDistributionGraph").doWrite(dataSheet);
            log.info("export entropyDistributionGraph graph success: " + outputFileName);
        } else {
            log.error("No spectra in library: {}", libraryId);
        }
    }

    public void simpleScoreGraph(String fileName, ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval, boolean bestHit, boolean logScale, int minLogScore) {
        String outputFileName = vmProperties.getRepository() + File.separator + fileName + ".xlsx";
        log.info("start export fake identification graph : " + outputFileName);
        //header
        List<Object> header = Arrays.asList("BeginScore", "EndScore", "Target", "Decoy");
        List<List<Object>> dataSheet = getSimpleDataSheet(hitsMap, scoreInterval, bestHit, logScale, minLogScore);
        dataSheet.add(0, header);
        EasyExcel.write(outputFileName).sheet("scoreGraph").doWrite(dataSheet);
        log.info("export simple identification graph success : " + outputFileName);
    }

    private List<List<Object>> getSimpleDataSheet(ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap, int scoreInterval, boolean bestHit, boolean logScale, int minLogScore) {
        List<List<Object>> dataSheet = new ArrayList<>();
        List<LibraryHit> decoyHits = new ArrayList<>();
        List<LibraryHit> targetHits = new ArrayList<>();
        hitsMap.forEach((k, v) -> {
            if (v.size() != 0) {
                Map<Boolean, List<LibraryHit>> decoyTargetMap = v.stream().collect(Collectors.groupingBy(LibraryHit::isDecoy));
                if (bestHit) {
                    //remain only the best hit
                    if (decoyTargetMap.get(true) != null && decoyTargetMap.get(true).size() != 0) {
                        decoyTargetMap.get(true).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        decoyHits.add(decoyTargetMap.get(true).get(0));
                    }
                    if (decoyTargetMap.get(false) != null && decoyTargetMap.get(false).size() != 0) {
                        decoyTargetMap.get(false).sort(Comparator.comparing(LibraryHit::getScore).reversed());
                        targetHits.add(decoyTargetMap.get(false).get(0));
                    }
                } else {
                    //remain all the hits
                    if (decoyTargetMap.get(true) != null) {
                        decoyHits.addAll(decoyTargetMap.get(true));
                    }
                    if (decoyTargetMap.get(false) != null) {
                        targetHits.addAll(decoyTargetMap.get(false));
                    }
                }
            }
        });

        //use score or log(score) as x-axis
        List<Double> thresholds = new ArrayList<>();
        if (logScale) {
            for (int i = 0; i < scoreInterval; i++) {
                thresholds.add(Math.pow(2, minLogScore + i * Math.abs(minLogScore) / (double) scoreInterval));
            }
        } else {
            for (int i = 0; i < scoreInterval; i++) {
                thresholds.add((double) i / scoreInterval);
            }
        }

        for (int i = 0; i < scoreInterval; i++) {
            double finalMinScore;
            if (i == 0) {
                finalMinScore = 0.0;
            } else {
                finalMinScore = thresholds.get(i);
            }
            double finalMaxScore;
            if (i == scoreInterval - 1) {
                finalMaxScore = 1.0;
            } else {
                finalMaxScore = thresholds.get(i + 1);
            }
            int targetCount, decoyCount;
            List<Object> row = new ArrayList<>();

            //calculate hits distribution
            if (i == 0) {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() >= finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            } else {
                targetCount = targetHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
                decoyCount = decoyHits.stream().filter(hit -> hit.getScore() > finalMinScore && hit.getScore() <= finalMaxScore).toList().size();
            }

            //write data sheet
            if (logScale) {
                row.add(minLogScore + i * Math.abs(minLogScore) / (double) scoreInterval);
                row.add(minLogScore + (i + 1) * Math.abs(minLogScore) / (double) scoreInterval);
            } else {
                row.add(finalMinScore);
                row.add(finalMaxScore);
            }
            row.add((double) targetCount / targetHits.size());
            if (decoyCount != 0) {
                row.add((double) decoyCount / decoyHits.size());
            } else {
                row.add(0.0);
            }
            dataSheet.add(row);
        }
        return dataSheet;
    }
}
