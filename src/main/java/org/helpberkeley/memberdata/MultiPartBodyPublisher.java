/*
 * Copyright (c) 2020. helpberkeley.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package org.helpberkeley.memberdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * The Class MultiPartBodyPublisher.
 */
public class MultiPartBodyPublisher {

    /** The parts specification list. */
    private final List<PartsSpecification> partsSpecificationList = new ArrayList<>();

    /** The boundary. */
     private final String boundary = UUID.randomUUID().toString();

    /**
     * Builds the.
     *
     * @return the http request. body publisher
     */
    public HttpRequest.BodyPublisher build() {
        if (partsSpecificationList.isEmpty()) {
            throw new IllegalStateException("Must have at least one part to build multipart message.");
        }
        addFinalBoundaryPart();
        return HttpRequest.BodyPublishers.ofByteArrays(PartsIterator::new);
    }

    /**
     * Gets the boundary.
     *
     * @return the boundary
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Adds the part.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @return the multi part body publisher
     */
    public MultiPartBodyPublisher addPart(String name, String value) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.STRING;
        newPart.name = name;
        newPart.value = value;
        partsSpecificationList.add(newPart);
        return this;
    }

    /**
     * Adds the part.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @return the multi part body publisher
     */
    public MultiPartBodyPublisher addParameterPart(String name, String value) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.PARAMETERS;
        newPart.name = name;
        newPart.value = value;
        partsSpecificationList.add(newPart);
        return this;
    }

    /**
     * Adds the part.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @return the multi part body publisher
     */
    public MultiPartBodyPublisher addParamPart(String name, String value) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.PARAM;
        newPart.name = name;
        newPart.value = value;
        partsSpecificationList.add(newPart);
        return this;
    }

    /**
     * Adds the part.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @return the multi part body publisher
     */
    public MultiPartBodyPublisher addPart(String name, Path value) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.FILE;
        newPart.name = name;
        newPart.path = value;
        partsSpecificationList.add(newPart);
        return this;
    }

    /**
     * Adds the part.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param filename
     *            the filename
     * @param contentType
     *            the content type
     * @return the multi part body publisher
     */
    public MultiPartBodyPublisher addPart(String name, Supplier<InputStream> value, String filename,
                                          String contentType) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.STREAM;
        newPart.name = name;
        newPart.stream = value;
        newPart.filename = filename;
        newPart.contentType = contentType;
        partsSpecificationList.add(newPart);
        return this;
    }

    /**
     * Adds the final boundary part.
     */
    private void addFinalBoundaryPart() {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.FINAL_BOUNDARY;
        newPart.value = "--" + boundary + "--\r\n";
        partsSpecificationList.add(newPart);
    }

    /**
     * The Class PartsSpecification.
     */
    static class PartsSpecification {

        /**
         * The Enum TYPE.
         */
        public enum TYPE {
            /** The string. */
            STRING,
            /** The file. */
            FILE,
            /** The stream. */
            STREAM,
            /** Query parameters */
            PARAMETERS,
            /** Single Query parameter */
            PARAM,
            /** The final boundary. */
            FINAL_BOUNDARY
        }

        /** The type. */
        PartsSpecification.TYPE type;

        /** The name. */
        String name;

        /** The value. */
        String value;

        /** The path. */
        Path path;

        /** The stream. */
        Supplier<InputStream> stream;

        /** The filename. */
        String filename;

        /** The content type. */
        String contentType;

    }

    /**
     * The Class PartsIterator.
     */
    class PartsIterator implements Iterator<byte[]> {

        /** The iter. */
        private final Iterator<PartsSpecification> iter;

        /** The current file input. */
        private InputStream currentFileInput;

        /** The done. */
        private boolean done;

        /** The next. */
        private byte[] next;

        /**
         * Instantiates a new parts iterator.
         */
        PartsIterator() {
            iter = partsSpecificationList.iterator();
        }

        /**
         * Checks for next.
         *
         * @return true, if successful
         */
        @Override
        public boolean hasNext() {
            if (done) {
                return false;
            }
            if (next != null) {
                return true;
            }
            try {
                next = computeNext();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (next == null) {
                done = true;
                return false;
            }
            return true;
        }

        /**
         * Next.
         *
         * @return the byte[]
         */
        @Override
        public byte[] next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            byte[] res = next;
            next = null;
            return res;
        }

        /**
         * Compute next.
         *
         * @return the byte[]
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private byte[] computeNext() throws IOException {
            if (currentFileInput == null) {
                if (!iter.hasNext()) {
                    return null;
                }
                PartsSpecification nextPart = iter.next();
                if (PartsSpecification.TYPE.STRING.equals(nextPart.type)) {
                    String part = "--" + boundary + "--\r\n" +
                            "Content-Disposition: form-data; name=\""
                            + nextPart.name + "\"\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                            nextPart.value + "\r\n";
                    return part.getBytes(StandardCharsets.UTF_8);
                }
                if (PartsSpecification.TYPE.PARAM.equals(nextPart.type)) {
                    String part = "--" + boundary + "--\r\n" +
                            "Content-Disposition: form-data; name=\""
                            + nextPart.name + "\"\r\n" +
                            "\r\n" +
                            nextPart.value + "\r\n";
                    return part.getBytes(StandardCharsets.UTF_8);
                }
                if (PartsSpecification.TYPE.PARAMETERS.equals(nextPart.type)) {
                    String part = "--" + boundary + "--\r\n" +
                            "Content-Disposition: form-data; name=\""
                            + nextPart.name + "\"\r\n" +
//                            "Content-Type: application/x-www-form-urlencoded; charset=UTF-8\r\n\r\n" +
                            "\r\n" +
                            nextPart.value + "\r\n";
                    return part.getBytes(StandardCharsets.UTF_8);
                }
                if (PartsSpecification.TYPE.FINAL_BOUNDARY.equals(nextPart.type)) {
                    return nextPart.value.getBytes(StandardCharsets.UTF_8);
                }
                String filename;
                String contentType;
                if (PartsSpecification.TYPE.FILE.equals(nextPart.type)) {
                    Path path = nextPart.path;
                    filename = path.getFileName().toString();
                    contentType = Files.probeContentType(path);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    currentFileInput = Files.newInputStream(path);
                } else {
                    filename = nextPart.filename;
                    contentType = nextPart.contentType;
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    currentFileInput = nextPart.stream.get();
                }
                String partHeader = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\""
                        + nextPart.name + "\"; filename=\""
                        + filename + "\"\r\n" +
                        "Content-Type: " + contentType + "\r\n\r\n";
                return partHeader.getBytes(StandardCharsets.UTF_8);
            } else {
                byte[] buf = new byte[8192];
                int r = currentFileInput.read(buf);
                if (r > 0) {
                    byte[] actualBytes = new byte[r];
                    System.arraycopy(buf, 0, actualBytes, 0, r);
                    return actualBytes;
                } else {
                    currentFileInput.close();
                    currentFileInput = null;
                    return "\r\n".getBytes(StandardCharsets.UTF_8);
                }
            }
        }
    }
}

