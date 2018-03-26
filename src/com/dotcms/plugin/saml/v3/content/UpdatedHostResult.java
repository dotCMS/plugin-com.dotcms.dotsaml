package com.dotcms.plugin.saml.v3.content;

import com.dotmarketing.beans.Host;

import java.io.Serializable;
import java.util.List;

public class UpdatedHostResult implements Serializable
{
	private static final long serialVersionUID = 2699494493924735669L;
	private final List<Host> updatedHosts;
	private final List<String> removedHosts;

	public UpdatedHostResult( List<Host> updatedHosts, List<String> removedHosts )
	{
		this.updatedHosts = updatedHosts;
		this.removedHosts = removedHosts;
	}

	public List<Host> getUpdatedHosts()
	{
		return updatedHosts;
	}

	public List<String> getRemovedHosts()
	{
		return removedHosts;
	}
}
