package com.dotcms.plugin.saml.v3.util.pagination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotcms.plugin.saml.v3.exception.DotSamlByPassException;
import com.dotcms.util.pagination.OrderDirection;
import com.dotcms.util.pagination.Paginator;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.liferay.portal.model.User;

public class IdpConfigPaginator implements Paginator<IdpConfig> {
	private final AtomicInteger lastTotalRecords = new AtomicInteger(0);

	public IdpConfigPaginator() {

	}

	@Override
	public long getTotalRecords(String s) {
		return lastTotalRecords.get();
	}

	@Override
	public Collection<IdpConfig> getItems(final User user, final String filter, final int limit, final int offset,
			final String orderby, final OrderDirection direction, final Map<String, Object> extraParams) {
		try {
			List<IdpConfig> idpConfigs = IdpConfigHelper.getInstance().getIdpConfigs();

			if (UtilMethods.isSet(filter)) {
				idpConfigs = idpConfigs.stream().filter(x -> x.contains(filter)).collect(Collectors.toList());
			}

			List<IdpConfig> paginatedAndFiltered = idpConfigs.stream().skip(offset).limit(limit)
					.collect(Collectors.toList());

			lastTotalRecords.set(idpConfigs.size());

			return paginatedAndFiltered;

		} catch (DotSamlByPassException exception) {

			Logger.error(this, exception.getMessage(), exception);
			lastTotalRecords.set(0);
			return new ArrayList<IdpConfig>();

		} catch (IOException | JSONException exception) {
			Logger.error(IdpConfigPaginator.class, "Error getting paginated IdpConfigs", exception);
			throw new DotRuntimeException(exception);
		}
	}
}
