//
// Copyright (c) 2024 helpberkeley.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package org.helpberkeley.memberdata;

import org.junit.Test;

import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.helpberkeley.memberdata.UploadFile.auditFilePrefix;


public class UploadFileTest extends TestBase {

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
        String filename = "[xyzzy.csv|attachment](" + "pload://" + "someRandomFileID" + ") (5.8 KB)";
        Throwable thrown = catchThrowable(() -> auditFilePrefix(filename));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(UploadFile.INVALID_FILE_PREFIX, filename));

    }




    @Test
    public void uploadResponseTest() {
        UploadResponse uploadResponse =
                HBParser.uploadResponse(readResourceFile("upload-response.json"));
        assertThat(uploadResponse.getFileName()).isEqualTo("x.csv");
        assertThat(uploadResponse.getShortURL()).isEqualTo("upload://6Gf8gG4nGnC7HiiEi6ZX79PFfX5.csv");
    }
}
