package net.csibio.mslibrary.client.domain.bean.peptide;

import lombok.Data;

import java.util.Set;

@Data
public class FragmentGroup {

    Double rt;
    Double mz;
    Set<FragmentInfo> fragments;
}
