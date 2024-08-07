package org.helpberkeley.memberdata;

import org.helpberkeley.memberdata.v300.ControlBlockV300;
import org.junit.Test;

import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.helpberkeley.memberdata.UploadFile.auditFilePrefix;


public class HBParserTest {

    @Test
    public void auditFilePrefixTestTrue1() {
        auditFilePrefix("[xyzzy.csv|attachment](" + Constants.UPLOAD_URI_PREFIX + "someRandomFileID" + ") (5.8 KB)");
    }
    @Test
    public void auditFilePrefixTestTrue2() {
        auditFilePrefix("[xyzzy.csv|attachment](" + Constants.WEB_CSV_PREFIX + "someRandomFileID" + ") (5.8 KB)");
    }
    @Test
    public void auditFilePrefixTestFails() {
        Throwable thrown = catchThrowable(() -> auditFilePrefix("[xyzzy.csv|attachment](" + "pload://" + "someRandomFileID" + ") (5.8 KB)"));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(ControlBlockV300.INVALID_FILE_PREFIX, "pload://"));

    }




}
