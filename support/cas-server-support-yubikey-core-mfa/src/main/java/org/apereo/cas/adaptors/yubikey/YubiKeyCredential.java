package org.apereo.cas.adaptors.yubikey;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Credential;

import java.io.Serializable;

/**
 * This is {@link YubiKeyCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyCredential implements Credential, Serializable {
    private static final long serialVersionUID = -7570600701132111037L;

    private String token;

    /**
     * Instantiates a new Yubi key credential.
     */
    public YubiKeyCredential() {
    }

    /**
     * Instantiates a new Yubi key credential.
     *
     * @param token the token
     */
    public YubiKeyCredential(final String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("token", this.token)
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof YubiKeyCredential)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final YubiKeyCredential other = (YubiKeyCredential) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.token, other.token);
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(97, 31);
        builder.append(this.token);
        return builder.toHashCode();
    }

    @Override
    public String getId() {
        return this.token;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.token);
    }
}
