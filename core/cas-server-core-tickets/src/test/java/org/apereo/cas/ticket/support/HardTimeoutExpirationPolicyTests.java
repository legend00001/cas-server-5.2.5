package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class HardTimeoutExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "hardTimeoutExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        final HardTimeoutExpirationPolicy policyWritten = new HardTimeoutExpirationPolicy();
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, HardTimeoutExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
