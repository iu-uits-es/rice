/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.core.mail;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.mail.EmailBcList;
import org.kuali.rice.core.api.mail.EmailBody;
import org.kuali.rice.core.api.mail.EmailCcList;
import org.kuali.rice.core.api.mail.EmailFrom;
import org.kuali.rice.core.api.mail.EmailSubject;
import org.kuali.rice.core.api.mail.EmailTo;
import org.kuali.rice.core.api.mail.EmailToList;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.core.api.mail.Mailer;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.krad.util.KRADConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Maintains a Java Mail session and is used for sending e-mails.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class MailerImpl implements Mailer, InitializingBean {

    protected final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MailerImpl.class);

    private JavaMailSenderImpl mailSender;
    private ParameterService parameterService;
    private KeyStore keyStore;
    private String keyStoreFile;
    private String keyStorePassword;

    @Override
    public void afterPropertiesSet() throws Exception {
        keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
    }

    /**
     * @param mailSender The injected Mail Sender.
     */
    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @param keyStoreFile The location on the filesystem where the keystore file is located
     */
    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    /**
     * @param keyStorePassword The password used to unlock the keystore
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Construct and a send simple email message from a Mail Message.
     *
     * @param message
     *            the Mail Message
     * @throws MessagingException
     */
    @Override
    @SuppressWarnings("unchecked")
    public void sendEmail(MailMessage message) throws MessagingException {

        // Construct a simple mail message from the Mail Message
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setTo( (String[])message.getToAddresses().toArray(new String[message.getToAddresses().size()]) );
        smm.setBcc( (String[])message.getBccAddresses().toArray(new String[message.getBccAddresses().size()]) );
        smm.setCc( (String[])message.getCcAddresses().toArray(new String[message.getCcAddresses().size()]) );
        smm.setSubject(message.getSubject());
        smm.setText(message.getMessage());
        smm.setFrom(message.getFromAddress());

        try {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "sendEmail() - Sending message: " + smm.toString() );
            }
            mailSender.send(smm);
        }
        catch (Exception e) {
            LOG.error("sendEmail() - Error sending email.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Send an email to a single recipient with the specified subject and message. This is a convenience
     * method for simple message addressing.
     *
     * @param from
     *            sender of the message
     * @param to
     *            list of addresses to which the message is sent
     * @param subject
     *            subject of the message
     * @param body
     *            body of the message
     */
    @Override
    public void sendEmail(EmailFrom from, EmailTo to, EmailSubject subject, EmailBody body, boolean htmlMessage) {
        if (to.getToAddress() == null) {
            LOG.warn("No To address specified. Refraining from sending mail.");
            return;
        }
        try {
            Address[] singleRecipient = {new InternetAddress(to.getToAddress())};
            sendMessage(from.getFromAddress(),
                    from.getReplyToAddress(),
                    singleRecipient,
                    subject.getSubject(),
                    body.getBody(),
                    null,
                    null,
                    htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail(): ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Send an email to the given "to", "cc", and "bcc" recipients with the specified subject and message.
     *
     * @param from
     *            sender of the message
     * @param to
     *            list of addresses to which the message is sent
     * @param subject
     *            subject of the message
     * @param body
     *            body of the message
     * @param cc
     *            list of addresses which are to be cc'd on the message
     * @param bc
     *            list of addresses which are to be bcc'd on the message
     */
    @Override
    public void sendEmail(EmailFrom from, EmailToList to, EmailSubject subject, EmailBody body, EmailCcList cc,
            EmailBcList bc, boolean htmlMessage) {
        if (to.getToAddresses().isEmpty()) {
            LOG.error("List of To addresses must contain at least one entry. Refraining from sending mail.");
            return;
        }
        try {
            sendMessage(from.getFromAddress(),
                    from.getReplyToAddress(),
                    to.getToAddressesAsAddressArray(),
                    subject.getSubject(),
                    body.getBody(),
                    (cc == null ? null : cc.getToAddressesAsAddressArray()),
                    (bc == null ? null : bc.getToAddressesAsAddressArray()),
                    htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail(): ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Send an email to the given recipients with the specified subject and message.
     *
     * @param from
     *            sender of the message
     * @param replyTo
     *            address to use for the message reply-to
     * @param to
     *            list of addresses to which the message is sent
     * @param subject
     *            subject of the message
     * @param messageBody
     *            body of the message
     * @param cc
     *            list of addresses which are to be cc'd on the message
     * @param bcc
     *            list of addresses which are to be bcc'd on the message
     */
    protected void sendMessage(String from, String replyTo, Address[] to, String subject, String messageBody, Address[] cc, Address[] bcc, boolean htmlMessage) throws AddressException, MessagingException, MailException, KeyStoreException {
        MimeMessage message = mailSender.createMimeMessage();

        // From Address
        message.setFrom(new InternetAddress(from));

        if (replyTo != null) {
            message.setReplyTo(new Address[]{new InternetAddress(replyTo)});
        }

        // To Address(es)
        if (to != null && to.length > 0) {
            message.addRecipients(Message.RecipientType.TO, to);
        } else {
            LOG.error("No recipients indicated.");
        }

        // CC Address(es)
        if (cc != null && cc.length > 0) {
            message.addRecipients(Message.RecipientType.CC, cc);
        }

        // BCC Address(es)
        if (bcc != null && bcc.length > 0) {
            message.addRecipients(Message.RecipientType.BCC, bcc);
        }

        // Subject
        message.setSubject(subject);
        if (subject == null || "".equals(subject)) {
            LOG.warn("Empty subject being sent.");
        }

        // Message body.
        if (htmlMessage) {
            prepareHtmlMessage(messageBody, message);
        } else {
            message.setText(messageBody);
            if (messageBody == null || "".equals(messageBody)) {
                LOG.warn("Empty message body being sent.");
            }
        }

        if (parameterService.getParameterValueAsBoolean(
                KewApiConstants.KEW_NAMESPACE,
                KRADConstants.DetailTypes.MAILER_DETAIL_TYPE,
                KewApiConstants.SIGN_EMAIL_INDICATOR
        )) {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                String keyPassword = ConfigContext.getCurrentContextConfig().getProperty(String.format(
                        "keystore.%s.password", StringUtils.substringBefore(alias, "@")));
                if (alias.equals(from) && StringUtils.isNotBlank(keyPassword)) {
                    message = signMessage(message, alias, keyPassword);
                }
            }
        }

        // Send the message
        try {
            mailSender.send(message);
        }
        catch (Exception e) {
            LOG.error("sendMessage(): ", e);
            throw new RuntimeException(e);
        }
    }

    protected MimeMessage signMessage(final MimeMessage message, final String alias, final String keyPassword) {
        try {
            PrivateKey privateKey = (PrivateKey)keyStore.getKey(alias, keyPassword.toCharArray());
            // Load certificate chain
            Certificate[] chain = keyStore.getCertificateChain(alias);

            MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();

            mailcap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
            mailcap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
            mailcap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
            mailcap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
            mailcap.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

            // Create the SMIMESignedGenerator
            SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
            capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
            capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
            capabilities.addCapability(SMIMECapability.dES_CBC);
            capabilities.addCapability(SMIMECapability.aES256_CBC);

            Session session = Session.getDefaultInstance(mailSender.getJavaMailProperties(),
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(alias, keyPassword);
                        }
                    });

            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            ASN1EncodableVector attributes = new ASN1EncodableVector();
            X500Name x500Name =  new X500Name(((X509Certificate) chain[0])
                    .getIssuerDN().getName());
            IssuerAndSerialNumber issuerAndSerialNumber = new IssuerAndSerialNumber(x500Name, ((X509Certificate) chain[0]).getSerialNumber());
            attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(issuerAndSerialNumber));
            attributes.add(new SMIMECapabilitiesAttribute(capabilities));

            SMIMESignedGenerator signer = new SMIMESignedGenerator();
            signer.addSigner(
                    privateKey,
                    (X509Certificate) chain[0],
                    "DSA".equals(privateKey.getAlgorithm()) ? SMIMESignedGenerator.DIGEST_SHA1
                            : SMIMESignedGenerator.DIGEST_MD5,
                    new AttributeTable(attributes), null);

            // Add the list of certs to the generator
            List<Certificate> certList = new ArrayList<Certificate>();
            certList.add(chain[0]);
            CertStore certs = CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(certList), "BC");
            signer.addCertificatesAndCRLs(certs);

            // Sign the message
            MimeMultipart mm = signer.generate(message, "BC");
            MimeMessage signedMessage = new MimeMessage(session);

            // Set all original MIME headers in the signed message
            Enumeration headers = message.getAllHeaderLines();
            while (headers.hasMoreElements()) {
                signedMessage.addHeaderLine((String) headers.nextElement());
            }

            // Set the content of the signed message
            signedMessage.setContent(mm);
            return signedMessage;
        } catch (KeyStoreException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        } catch (UnrecoverableKeyException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        } catch (NoSuchProviderException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        } catch (SMIMEException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        } catch (CertStoreException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        } catch (InvalidAlgorithmParameterException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        } catch (MessagingException e) {
            LOG.error("Exception while signing message. Message will be sent unsigned", e);
        }

        return message;
    }

    protected void prepareHtmlMessage(String messageText, Message message) throws MessagingException {
        try {
            message.setDataHandler(new DataHandler(new ByteArrayDataSource(messageText, "text/html")));
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
