package org.apereo.cas.adaptors.x509.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;


/**
 * Provides a simple HTTP Web server that can serve out a single resource for
 * all requests.  SSL/TLS is not supported.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class MockWebServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockWebServer.class);
    
    /** Request handler. */
    private final Worker worker;

    /** Controls the worker thread. */
    private Thread workerThread;


    /**
     * Creates a new server that listens for requests on the given port and
     * serves the given resource for all requests.
     *
     * @param port Server listening port.
     * @param resource Resource to serve.
     * @param contentType MIME content type of resource to serve.
     */
    public MockWebServer(final int port, final Resource resource, final String contentType) {
        try {
            this.worker = new Worker(new ServerSocket(port), resource, contentType);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot create Web server", e);
        }
    }

    /** Starts the Web server so it can accept requests on the listening port. */
    public void start() {
        this.workerThread = new Thread(this.worker, "MockWebServer.Worker");
        this.workerThread.start();
    }

    /** Stops the Web server after processing any pending requests. */
    public void stop() {
        if (!isRunning()) {
            return;
        }
        this.worker.stop();
        try {
            this.workerThread.join();
        } catch (final InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Determines whether the server is running or not.
     *
     * @return True if server is running, false otherwise.
     */
    public boolean isRunning() {
        return this.workerThread.isAlive();
    }

    /**
     * Worker class handles request processing.
     */
    private static class Worker implements Runnable {

        /** Server always returns HTTP 200 response. */
        private static final String STATUS_LINE = "HTTP/1.1 200 Success\r\n";

        /** Separates HTTP header from body. */
        private static final String SEPARATOR = "\r\n";

        /** Response buffer size. */
        private static final int BUFFER_SIZE = 2048;

        private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);
        
        /** Run flag. */
        private boolean running;

        /** Server socket. */
        private final ServerSocket serverSocket;

        /** Resource to serve. */
        private final Resource resource;

        /** MIME content type of resource to serve. */
        private final String contentType;


        /**
         * Creates a request-handling worker that listens for requests on the
         * given socket and serves the given resource for all requests.
         *
         * @param sock Server socket.
         * @param resource Single resource to serve.
         * @param contentType MIME content type of resource to serve.
         */
        Worker(final ServerSocket sock, final Resource resource, final String contentType) {
            this.serverSocket = sock;
            this.resource = resource;
            this.contentType = contentType;
            this.running = true;
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    writeResponse(this.serverSocket.accept());
                    Thread.sleep(100);
                } catch (final SocketException e) {
                    LOGGER.debug("Stopping on socket close.");
                    this.running = false;
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        public void stop() {
            try {
                this.serverSocket.close();
            } catch (final IOException e) {
                LOGGER.trace("Exception when closing the server socket: [{}]", e.getMessage());
            }
        }

        private void writeResponse(final Socket socket) throws IOException {
            LOGGER.debug("Socket response for resource [{}]", resource.getFilename());
            final OutputStream out = socket.getOutputStream();
            out.write(STATUS_LINE.getBytes());
            out.write(header("Content-Length", this.resource.contentLength()));
            out.write(header("Content-Type", this.contentType));
            out.write(SEPARATOR.getBytes());

            final byte[] buffer = new byte[BUFFER_SIZE];
            try(InputStream in = this.resource.getInputStream()) {
                int count = 0;
                while ((count = in.read(buffer)) > -1) {
                    out.write(buffer, 0, count);
                }
            }
            LOGGER.debug("Wrote response for resource [{}] for [{}]",
                    resource.getFilename(),
                    resource.contentLength());

            socket.shutdownOutput();
        }

        private static byte[] header(final String name, final Object value) {
            return String.format("%s: %s\r\n", name, value).getBytes();
        }
    }
}
