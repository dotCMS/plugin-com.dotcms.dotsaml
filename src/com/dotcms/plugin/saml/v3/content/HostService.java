package com.dotcms.plugin.saml.v3.content;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.HostAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is kinda stateful service to keep the tracking of the hosts updates.
 * @author jsanca
 */
public class HostService {


    private final HostAPI hostAPI = APILocator.getHostAPI();
    private final UserAPI userAPI = APILocator.getUserAPI();
    private final Map<String, Date> hostByModDateMap =
            new ConcurrentHashMap<>();

    /**
     * Get All hosts, not matter if they are the updated or not.
     *
     * @return List of Host
     * @throws DotDataException
     * @throws DotSecurityException
     */
    public List<Host> getAllHosts () throws DotDataException, DotSecurityException {

        final List<Host> hosts    =
                this.hostAPI.findAllFromDB(this.userAPI.getSystemUser(), false);

        if (null != hosts) {

            this.hostByModDateMap.clear();
            hosts.forEach(host -> this.hostByModDateMap.put
                            (host.getHostname(), host.getModDate()));
        }

        return hosts;
    } // getAllHosts.

    /**
     * Get all updated host from last time they were requests.
     * @return
     * @throws DotDataException
     * @throws DotSecurityException
     */
    public List<Host> getUpdatedHosts () throws DotDataException, DotSecurityException {

        final List<Host> hosts    =
                this.hostAPI.findAllFromDB(this.userAPI.getSystemUser(), false);
        final List<Host> updatedHosts =
                new ArrayList<>(); // todo: make me immutable on 4.x

        if (null != hosts) {

            for (final Host host : hosts) {

                if (!this.hostByModDateMap.containsKey(host.getHostname()) || // if it is new
                        (!this.hostByModDateMap.get(host.getHostname()).equals(host.getModDate()))) { // or the mod update is not the same (means it is updated)

                    updatedHosts.add(host);
                    this.hostByModDateMap.put
                            (host.getHostname(), host.getModDate());
                }
            }
        }

        return updatedHosts;
    } // getUpdatedHosts.


    public Host findDefaultHost(final String fallbackSite) throws DotDataException, DotSecurityException {

        //Verify if a fallback site is configured and get its SAML configuration
        return (fallbackSite != null && !fallbackSite.isEmpty())?
                this.hostAPI.findByName(fallbackSite, this.userAPI.getSystemUser(), false)
                // if not fallback use the default host
                :this.hostAPI.findDefaultHost(this.userAPI.getSystemUser(), false);
    } // findDefaultHost.

    /**
     * Get Host Alias
     * @param host Host
     * @return List of String alias
     */
    public List<String> getHostAlias (final Host host) {

        return this.hostAPI.parseHostAliases(host);
    } // getHostAlias.


} // E:O:F:HostService.
