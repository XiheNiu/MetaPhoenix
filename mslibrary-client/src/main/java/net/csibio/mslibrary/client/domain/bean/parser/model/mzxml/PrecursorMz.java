package net.csibio.mslibrary.client.domain.bean.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import lombok.Data;
import net.csibio.mslibrary.client.domain.bean.parser.xml.PrecursorMzConverter;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-06 09:18
 */
@Data
@XStreamConverter(PrecursorMzConverter.class)
public class PrecursorMz {

    Float value;

    @XStreamAsAttribute
    Long precursorScanNum;

    @XStreamAsAttribute
    Float precursorIntensity;

    @XStreamAsAttribute
    Integer precursorCharge;

    @XStreamAsAttribute
    String possibleCharges;

    @XStreamAsAttribute
    Float windowWideness;

    @XStreamAsAttribute
    String activationMethod;
}
