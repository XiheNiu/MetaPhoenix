package net.csibio.metaphoenix.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.db.LibraryDO;
import net.csibio.metaphoenix.client.domain.query.LibraryQuery;
import net.csibio.metaphoenix.client.exceptions.XException;
import net.csibio.metaphoenix.client.service.CompoundService;
import net.csibio.metaphoenix.client.service.IDAO;
import net.csibio.metaphoenix.client.service.LibraryService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import net.csibio.metaphoenix.core.dao.LibraryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service("libraryService")
public class LibraryServiceImpl implements LibraryService {

    @Autowired
    LibraryDAO libraryDAO;
    @Autowired
    CompoundService compoundService;
    @Autowired
    SpectrumService spectrumService;

    @Override
    public long count(LibraryQuery query) {
        return libraryDAO.count(query);
    }

    @Override
    public IDAO<LibraryDO, LibraryQuery> getBaseDAO() {
        return libraryDAO;
    }

    @Override
    public void beforeInsert(LibraryDO library) throws XException {
        library.setCreateDate(new Date());
        library.setLastModifiedDate(new Date());
        library.setId(library.getName());
    }

    @Override
    public void beforeUpdate(LibraryDO library) throws XException {
        library.setLastModifiedDate(new Date());
    }

    @Override
    public void beforeRemove(String id) throws XException {
        compoundService.removeAllByLibraryId(id);
    }

    @Override
    public List<LibraryDO> getAllByIds(List<String> ids) {
        try {
            return libraryDAO.getAllByIds(ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result removeAll() {
        libraryDAO.remove(new LibraryQuery());
        log.info("所有库已经被删除");
        return new Result(true);
    }
}
