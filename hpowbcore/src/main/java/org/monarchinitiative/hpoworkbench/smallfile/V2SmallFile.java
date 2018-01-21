package org.monarchinitiative.hpoworkbench.smallfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 1/20/2018.
 */
public class V2SmallFile {

    List<V2SmallFileEntry> entryList=new ArrayList<>();

    public V2SmallFile(OldSmallFile osf) {
        List<OldSmallFileEntry> oldlist = osf.getEntrylist();
        for (OldSmallFileEntry oldentry : oldlist) {
            V2SmallFileEntry v2entry = new V2SmallFileEntry(oldentry);
            entryList.add(v2entry);
        }
    }


}
