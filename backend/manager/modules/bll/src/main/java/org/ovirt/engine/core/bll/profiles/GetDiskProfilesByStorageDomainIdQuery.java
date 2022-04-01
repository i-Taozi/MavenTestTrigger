package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

public class GetDiskProfilesByStorageDomainIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private DiskProfileDao diskProfileDao;

    public GetDiskProfilesByStorageDomainIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskProfileDao
                .getAllForStorageDomain(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }

}
