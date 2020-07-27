package com.dotmarketing.business;

import com.dotcms.auth.providers.jwt.factories.ApiTokenCache;
import com.dotcms.business.SystemCache;
import com.dotcms.cache.KeyValueCache;
import com.dotcms.cache.KeyValueCacheImpl;

import com.dotcms.vanityurl.cache.VanityUrlCache;
import com.dotcms.vanityurl.cache.VanityUrlCacheImpl;
import com.dotcms.content.elasticsearch.ESQueryCache;
import com.dotcms.content.elasticsearch.business.IndiciesCache;
import com.dotcms.content.elasticsearch.business.IndiciesCacheImpl;
import com.dotcms.contenttype.business.ContentTypeCache2;
import com.dotcms.contenttype.business.ContentTypeCache2Impl;
import com.dotcms.csspreproc.CSSCache;
import com.dotcms.csspreproc.CSSCacheImpl;
import com.dotcms.notifications.business.NewNotificationCache;
import com.dotcms.notifications.business.NewNotificationCacheImpl;
import com.dotcms.publisher.assets.business.PushedAssetsCache;
import com.dotcms.publisher.assets.business.PushedAssetsCacheImpl;
import com.dotcms.publisher.endpoint.business.PublishingEndPointCache;
import com.dotcms.publisher.endpoint.business.PublishingEndPointCacheImpl;
import com.dotcms.rendering.velocity.services.DotResourceCache;
import com.dotcms.rendering.velocity.viewtools.navigation.NavToolCache;
import com.dotcms.rendering.velocity.viewtools.navigation.NavToolCacheImpl;

import com.dotcms.security.apps.AppsCache;
import com.dotcms.security.apps.AppsCacheImpl;


import com.dotmarketing.business.cache.transport.CacheTransport;
import com.dotmarketing.business.portal.PortletCache;
import com.dotmarketing.cache.ContentTypeCache;
import com.dotmarketing.cache.FolderCache;
import com.dotmarketing.cache.FolderCacheImpl;
import com.dotmarketing.cache.LegacyContentTypeCacheImpl;
import com.dotmarketing.cache.MultiTreeCache;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.logConsole.model.LogMapperCache;
import com.dotmarketing.logConsole.model.LogMapperCacheImpl;
import com.dotmarketing.plugin.business.PluginCache;
import com.dotmarketing.plugin.business.PluginCacheImpl;
import com.dotmarketing.portlets.categories.business.CategoryCache;
import com.dotmarketing.portlets.categories.business.CategoryCacheImpl;
import com.dotmarketing.portlets.containers.business.ContainerCache;
import com.dotmarketing.portlets.containers.business.ContainerCacheImpl;
import com.dotmarketing.portlets.contentlet.business.ContentletCache;
import com.dotmarketing.portlets.contentlet.business.ContentletCacheImpl;
import com.dotmarketing.portlets.contentlet.business.HostCache;
import com.dotmarketing.portlets.contentlet.business.HostCacheImpl;
import com.dotmarketing.portlets.hostvariable.bussiness.HostVariablesCache;
import com.dotmarketing.portlets.hostvariable.bussiness.HostVariablesCacheImpl;
import com.dotmarketing.portlets.htmlpages.business.HTMLPageCache;
import com.dotmarketing.portlets.htmlpages.business.HTMLPageCacheImpl;
import com.dotmarketing.portlets.languagesmanager.business.LanguageCache;
import com.dotmarketing.portlets.languagesmanager.business.LanguageCacheImpl;
import com.dotmarketing.portlets.links.business.MenuLinkCache;
import com.dotmarketing.portlets.links.business.MenuLinkCacheImpl;
import com.dotmarketing.portlets.rules.business.RulesCache;
import com.dotmarketing.portlets.rules.business.RulesCacheImpl;
import com.dotmarketing.portlets.rules.business.SiteVisitCache;
import com.dotmarketing.portlets.rules.business.SiteVisitCacheImpl;
import com.dotmarketing.portlets.structure.factories.RelationshipCache;
import com.dotmarketing.portlets.structure.factories.RelationshipCacheImpl;
import com.dotmarketing.portlets.templates.business.TemplateCache;
import com.dotmarketing.portlets.templates.business.TemplateCacheImpl;
import com.dotmarketing.portlets.workflows.business.WorkflowCache;
import com.dotmarketing.portlets.workflows.business.WorkflowCacheImpl;
import com.dotmarketing.tag.business.TagCache;
import com.dotmarketing.tag.business.TagCacheImpl;
import com.dotmarketing.tag.business.TagInodeCache;
import com.dotmarketing.tag.business.TagInodeCacheImpl;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.WebKeys;


/**
 *
 * @author Carlos Rivas (crivas)
 * @author Jason Tesser
 * @version 1.6
 * @since 1.6
 *
 */
enum CacheIndex
{
	System("System"),
	Permission("Permission"),
	CMSRole("CMS Role"),
	Role("Role"),
	Category("Category"),
	Tag("Tag"),
	TagInode("TagInode"),
	Contentlet("Contentlet"),
	LogMapper("LogMapper"),
	Relationship("Relationship"),
	Plugin("Plugin"),
	Language("Language"),
	User("User"),
	Layout("Layout"),
	Userproxy("User Proxy"),
	Host("Host"),
	HTMLPage("Page"),
	Menulink("Menu Link"),
	Container("Container"),
	Template("Template"),
	Identifier("Identifier"),
	Versionable("Versionable"),
	FolderCache("FolderCache"),
	WorkflowCache("Workflow Cache"),
	HostVariables("Host Variables"),
	Block_Directive("Block Directive"),
	Block_Page("Block Page"),
	Indicies("Indicies"),
	NavTool("Navigation Tool"),
	PublishingEndPoint("PublishingEndPoint Cache"),
	PushedAssets("PushedAssets Cache"),
	CSSCache("Processed CSS Cache"),
	RulesCache("Rules Cache"),
	SiteVisitCache("Rules Engine - Site Visits"),
	NewNotification("NewNotification Cache"),
	VanityURLCache("Vanity URL Cache"),
	ContentTypeCache("Legacy Content Type Cache"),
	ContentTypeCache2("New Content Type Cache"),
	Velocity2("Velocity2"),
	NavTool2("Navigation Tool2"),
	MultiTreeCache("MultiTree Cache"),
	ApiTokenCache("ApiTokenCache"),
	PortletCache("PortletCache"),
	ESQueryCache("ESQueryCache"),
	KeyValueCache("Key/Value Cache"),
	AppsCache("Apps");

	Cachable create() {
		switch(this) {
			case System: return new SystemCache();
			case Permission: return new PermissionCacheImpl();
			case Category: return new CategoryCacheImpl();
			case Tag: return new TagCacheImpl();
			case TagInode: return new TagInodeCacheImpl();
			case Role: return new RoleCacheImpl();
			case Contentlet: return new ContentletCacheImpl();
			case Velocity2 : return new DotResourceCache();
			case Relationship: return new RelationshipCacheImpl();
			case LogMapper: return new LogMapperCacheImpl();
			case Plugin : return new PluginCacheImpl();
			case Language : return new LanguageCacheImpl();
			case User : return new UserCacheImpl();
			case Userproxy : return new UserProxyCacheImpl();
			case Layout : return new LayoutCacheImpl();
			case CMSRole : return new com.dotmarketing.business.RoleCacheImpl();
			case HTMLPage : return new HTMLPageCacheImpl();
			case Menulink : return new MenuLinkCacheImpl();
			case Container : return new ContainerCacheImpl();
			case Template : return new TemplateCacheImpl();
			case Host : return new HostCacheImpl();
			case Identifier : return new IdentifierCacheImpl();
			case HostVariables : return new HostVariablesCacheImpl();
			case Block_Directive : return new BlockDirectiveCacheImpl();
			case Block_Page : return new BlockPageCacheImpl();
			case Versionable : return new VersionableCacheImpl();
			case FolderCache : return new FolderCacheImpl();
			case WorkflowCache : return new WorkflowCacheImpl();
			case Indicies: return new IndiciesCacheImpl();
			case NavTool: return new NavToolCacheImpl();
			case PublishingEndPoint: return new PublishingEndPointCacheImpl();
			case PushedAssets: return new PushedAssetsCacheImpl();
			case CSSCache: return new CSSCacheImpl();
			case NewNotification: return new NewNotificationCacheImpl();
			case RulesCache : return new RulesCacheImpl();
			case SiteVisitCache : return new SiteVisitCacheImpl();
			case ContentTypeCache: return new LegacyContentTypeCacheImpl();
			case ContentTypeCache2: return new ContentTypeCache2Impl();
			case VanityURLCache : return new VanityUrlCacheImpl();
			case KeyValueCache : return new KeyValueCacheImpl();
			case MultiTreeCache : return new MultiTreeCache();
			case ApiTokenCache : return new ApiTokenCache();
			case PortletCache : return new PortletCache();
			case AppsCache: return new AppsCacheImpl();
			case ESQueryCache : return new com.dotcms.content.elasticsearch.ESQueryCache();

		}
		throw new AssertionError("Unknown Cache index: " + this);
	}

	private String value;

	CacheIndex (String value) {
		this.value = value;
	}

	public String toString () {
		return value;
	}

	public static CacheIndex getCacheIndex (String value) {
		CacheIndex[] types = CacheIndex.values();
		for (CacheIndex type : types) {
			if (type.value.equals(value))
				return type;
		}
		return null;
	}

}
