package net.csibio.mslibrary.client.filter;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.Constants;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.ArrayUtil;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("noiseFilter")
@Slf4j
public class NoiseFilter {
    @Autowired
    SpectrumService spectrumService;

    public void filter(String libraryId) {
        log.info("start noise filter on library: {}", libraryId);
        List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);
        int count = spectrumDOS.size();

        //1. remove spectra with empty key information
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getSmiles() == null || spectrumDO.getSmiles().equals("") || spectrumDO.getMzs() == null || spectrumDO.getInts() == null || spectrumDO.getMzs().length == 0 || spectrumDO.getInts().length == 0);
        log.info("remove {} spectra with empty key information, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //2. remove low resolution data (the precursorMz is not in the spectrum or the difference between the precursorMz and the nearest m/z is larger than 10ppm)
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getPrecursorMz() == null || spectrumDO.getPrecursorMz() == 0 || ArrayUtil.findNearestDiff(spectrumDO.getMzs(), spectrumDO.getPrecursorMz()) > 10 * Constants.PPM * spectrumDO.getPrecursorMz());
        log.info("remove {} spectra with low resolution, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //3. remove noise data points (intensity < 0.01 * basePeakIntensity) and normalize intensity
        int dataPoint = 0;
        int totalDataPoint = 0;
        for (SpectrumDO spectrumDO : spectrumDOS) {
            double basePeakIntensity = StatUtils.max(spectrumDO.getInts());
            List<Double> mzs = new ArrayList<>();
            List<Double> ints = new ArrayList<>();
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                if (spectrumDO.getInts()[i] >= 0.01 * basePeakIntensity) {
                    mzs.add(spectrumDO.getMzs()[i]);
                    ints.add(spectrumDO.getInts()[i] / basePeakIntensity * 100);
                }
            }
            dataPoint += spectrumDO.getMzs().length - mzs.size();
            totalDataPoint += spectrumDO.getMzs().length;
            spectrumDO.setMzs(mzs.stream().mapToDouble(Double::doubleValue).toArray());
            spectrumDO.setInts(ints.stream().mapToDouble(Double::doubleValue).toArray());
        }
        log.info("remove {} noise data points, {} spectra left, {}% data points removed", dataPoint, spectrumDOS.size(), dataPoint * 100.0 / totalDataPoint);

        //4. remove spectra with <5 peaks with relative intensity above 2%
        spectrumDOS.removeIf(spectrumDO -> {
            int goodIons = 0;
            double maxIntensity = StatUtils.max(spectrumDO.getInts());
            for (double value : spectrumDO.getInts()) {
                if (value / maxIntensity > 0.02) {
                    goodIons++;
                }
            }
            return goodIons < 5;
        });
        log.info("remove {} spectra with <5 peaks with relative intensity above 2%, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());
        count = spectrumDOS.size();

        //5. remove spectra with ion count > 300
        spectrumDOS.removeIf(spectrumDO -> spectrumDO.getMzs().length > 300);
        log.info("remove {} spectra with ion count > 300, {} spectra left", count - spectrumDOS.size(), spectrumDOS.size());

        spectrumService.remove(new SpectrumQuery(), libraryId);
        spectrumService.insert(spectrumDOS, libraryId);
        log.info("finish noise filter on library: {}", libraryId);
    }
}
