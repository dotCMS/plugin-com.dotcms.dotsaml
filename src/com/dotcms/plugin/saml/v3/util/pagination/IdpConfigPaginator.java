package com.dotcms.plugin.saml.v3.util.pagination;

import com.dotcms.plugin.saml.v3.rest.api.v1.IdpConfig;
import com.dotcms.plugin.saml.v3.rest.api.v1.IdpConfigWriterReader;
import com.dotcms.util.pagination.OrderDirection;
import com.dotcms.util.pagination.Paginator;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;
import com.liferay.portal.model.User;
import com.liferay.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IdpConfigPaginator implements Paginator<IdpConfig> {

    private final AtomicInteger lastTotalRecords = new AtomicInteger(0);

    private final String assetsPath;
    private final String idpfilePath;

    public IdpConfigPaginator() {
        this.assetsPath = Config.getStringProperty("ASSET_REAL_PATH",
            FileUtil.getRealPath(Config.getStringProperty("ASSET_PATH", "/assets")));
        this.idpfilePath = assetsPath + File.separator + "saml" + File.separator + "config.json";
    }

    @Override
    public long getTotalRecords(String s) {
        return lastTotalRecords.get();
    }

    @Override
    public Collection<IdpConfig> getItems(final User user, final String filter, final int limit, final int offset,
                                          final String orderby, final OrderDirection direction,
                                          final Map<String, Object> extraParams) {

        try {
            final List<IdpConfig> idpConfigs = IdpConfigWriterReader.readIdpConfigs(new File(idpfilePath));
            final List<IdpConfig> paginated = idpConfigs.stream().skip(offset).limit(limit).collect(Collectors.toList());

            lastTotalRecords.set(idpConfigs.size());

            return paginated;

        } catch (IOException | JSONException e) {
            Logger.error(IdpConfigPaginator.class, "Error getting paginated IdpConfigs", e);
            throw new DotRuntimeException(e);
        }
    }
}
