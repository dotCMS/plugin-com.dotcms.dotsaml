package com.dotcms.plugin.saml.v3.util.pagination;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;

import com.dotcms.util.pagination.OrderDirection;
import com.dotcms.util.pagination.PaginationException;
import com.dotcms.util.pagination.Paginator;

import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.PaginatedArrayList;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;

import com.liferay.portal.model.User;
import com.liferay.util.StringPool;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IdpConfigPaginator implements Paginator<IdpConfig>
{
	private final AtomicInteger lastTotalRecords = new AtomicInteger( 0 );

	public IdpConfigPaginator()
	{

	}

	@Override
	public PaginatedArrayList<IdpConfig> getItems(final User user, final int limit, final int offset, final
	Map<String, Object> params) throws
			PaginationException {
		try {
			final PaginatedArrayList<IdpConfig> result = new PaginatedArrayList<>();
			List<IdpConfig> idpConfigs = IdpConfigHelper.getInstance().getIdpConfigs();
			final String filter = (null != params && null != params.get(Paginator.DEFAULT_FILTER_PARAM_NAME)) ? String
					.class.cast(params.get(Paginator.DEFAULT_FILTER_PARAM_NAME)) : StringPool.BLANK;
			if (UtilMethods.isSet(filter)) {
				idpConfigs = idpConfigs.stream()
						.filter(x -> x.contains(filter))
						.collect(Collectors.toList());
			}
			final List<IdpConfig> paginatedAndFiltered = idpConfigs.stream().skip(offset).limit(limit).collect
					(Collectors.toList());
			lastTotalRecords.set(idpConfigs.size());
			paginatedAndFiltered.stream().forEach(x -> result.add(x));
			return result;
		} catch (final IOException | JSONException exception) {
			final String errorMsg = "Error getting paginated IdpConfigs for user '" + user.getUserId() + "': " +
					exception.getMessage();
			Logger.error(IdpConfigPaginator.class, errorMsg, exception);
			throw new DotRuntimeException(errorMsg, exception);
		}
	}

}
