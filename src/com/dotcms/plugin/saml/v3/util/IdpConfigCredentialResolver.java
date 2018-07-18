package com.dotcms.plugin.saml.v3.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import org.opensaml.xml.util.Base64;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.AbstractCriteriaFilteringCredentialResolver;
import org.opensaml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.json.JSONException;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

/**
 * <strong>NOTE:</strong> this class is a non-standard, custom implementation of
 * the proposed FilesystemCredentialResolver A
 * {@link org.opensaml.security.credential.CredentialResolver} that pulls
 * credential information from the file system.
 * 
 * This credential resolver attempts to retrieve credential information from the
 * file system or dotSAML cache. Specifically it will attempt to find the
 * IdpConfig data file and use it to populate BasicX509Credentials including the
 * entityID and public and private key data.
 * 
 */
public class IdpConfigCredentialResolver extends AbstractCriteriaFilteringCredentialResolver {

	/** Class logger. */
	private final Logger log = LoggerFactory.getLogger(IdpConfigCredentialResolver.class);

	private final IdpConfigHelper helper;

	/**
	 * Constructor.
	 * 
	 * @param configId
	 *            The IdpConfig ID to retrieve.
	 */
	public IdpConfigCredentialResolver() {
		super();
		helper = IdpConfigHelper.getInstance();
	}

	/** {@inheritDoc} */
	@Nonnull
	protected Iterable<Credential> resolveFromSource(@Nullable final CriteriaSet criteriaSet) throws ResolverException {

		checkCriteriaRequirements(criteriaSet);
		String entityID = criteriaSet.get(EntityIdCriterion.class).getEntityId();
		IdpConfig idpConfig = getIdpConfig(entityID);

		X509Certificate cert = getPublicCert(idpConfig.getPublicCert());
		PrivateKey privateKey = getPriavteKey(idpConfig.getPrivateKey());

		BasicX509Credential credential = new BasicX509Credential(cert, privateKey);
		credential.setEntityId(idpConfig.getId());
		credential.setUsageType(UsageType.UNSPECIFIED);

		ArrayList<X509Certificate> certChain = new ArrayList<>();
		certChain.add(cert);
		credential.setEntityCertificateChain(certChain);

		return Collections.singleton(credential);
	}

	/**
	 * Check that required credential criteria are available.
	 * 
	 * @param criteriaSet
	 *            the credential criteria set to evaluate
	 */
	protected void checkCriteriaRequirements(@Nullable final CriteriaSet criteriaSet) {

		if (criteriaSet == null || criteriaSet.get(EntityIdCriterion.class) == null) {

			log.error("EntityIDCriterion was not specified in the criteria set, resolution cannot be attempted");
			throw new IllegalArgumentException("No EntityIDCriterion was available in criteria set");
		}
	}

	protected IdpConfig getIdpConfig(final String id) throws ResolverException {

		IdpConfig idpConfig = null;

		try {

			idpConfig = helper.findIdpConfig(id);

		} catch (final IOException e) {

			log.error("Unable to read IdpConfig data for ID: {}", id);
			throw new ResolverException("Unable to read IdpConfig data for ID", e);

		} catch (final JSONException e) {

			log.error("JSON Exception while reading IdpConfig data for ID: {}", id);
			throw new ResolverException("JSON Exception while reading IdpConfig data", e);

		} catch (final DotDataException e) {

			log.error("DotData Exception while reading IdpConfig data for ID: {}", id);
			throw new ResolverException("DotData Exception while reading IdpConfig data", e);

		}

		if (idpConfig == null) {
			log.error("Unable to located IdpConfig file with ID: {}", id);
			throw new ResolverException("Unable to located IdpConfig file with ID:");
		}

		return idpConfig;
	}

	protected X509Certificate getPublicCert(final File certFile) throws ResolverException {

		X509Certificate cert = null;

		if (certFile == null) {
			log.error("Public Key file cannot be null!");
			throw new ResolverException("Public Key file cannot be null!");
		}

		if (!certFile.exists()) {
			log.error("Public Key file must Exist!");
			throw new ResolverException("Public Key file must Exist!");
		}

		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) cf.generateCertificate(new FileInputStream(certFile));

		} catch (IOException e) {
			log.error("Unable to read Public Key File");
			throw new ResolverException("Unable to read Public Key File", e);

		} catch (CertificateException e) {
			log.error("Certificate Error reading Public Key File");
			throw new ResolverException("Certificate Error reading Public Key File", e);

		}

		if (cert == null) {
			log.error("Public certificate cannot be null!");
			throw new ResolverException("Public certificate cannot be null!");
		}

		return cert;
	}

	protected PrivateKey getPriavteKey(final File keyFile) throws ResolverException {

		PrivateKey privKey = null;

		if (keyFile == null) {
			log.error("Private Key file cannot be null!");
			throw new ResolverException("Private Key file cannot be null!");
		}

		if (!keyFile.exists()) {
			log.error("Private Key file must Exist!");
			throw new ResolverException("Private Key file must Exist!");
		}

		try {
			// TODO This locks in the private key type to RSA. We will need to
			// review.
			String stringPrivateKey = FileUtils.readFileToString(keyFile, Charset.forName("utf-8"));
			stringPrivateKey = stringPrivateKey.replace("-----BEGIN PRIVATE KEY-----\n", "");
			stringPrivateKey = stringPrivateKey.replace("-----END PRIVATE KEY-----", "");

			KeyFactory kf = KeyFactory.getInstance("RSA");

			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(stringPrivateKey));
			privKey = kf.generatePrivate(keySpecPKCS8);

		} catch (IOException e) {
			log.error("Unable to read Private Key File");
			throw new ResolverException("Unable to read Private Key File", e);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			log.error("Unable to translate Private Key");
			throw new ResolverException("Unable to translate Private Key", e);
		}

		if (privKey == null) {
			log.error("Private certificate cannot be null!");
			throw new ResolverException("Private certificate cannot be null!");
		}

		return privKey;
	}

}