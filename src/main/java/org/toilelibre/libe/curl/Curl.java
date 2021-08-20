package org.toilelibre.libe.curl;

import org.apache.commons.cli.*;
import org.apache.http.*;
import org.apache.http.conn.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static org.toilelibre.libe.curl.Curl.CurlArgumentsBuilder.CurlJavaOptions.*;
import static org.toilelibre.libe.curl.UglyVersionDisplay.*;

public final class Curl {

    private Curl () {
    }

    public static String $ (final String requestCommand) throws CurlException {
        return $ (requestCommand, with ().build ());
    }

    public static String $ (final String requestCommand, CurlArgumentsBuilder.CurlJavaOptions curlJavaOptions) throws CurlException {
        try {
            return IOUtils.quietToString (Curl.curl (requestCommand, curlJavaOptions).getEntity ());
        } catch (final UnsupportedOperationException e) {
            throw new CurlException (e);
        }
    }

    public static CompletableFuture<String> $Async (final String requestCommand) throws CurlException {
        return $Async (requestCommand, with ().build ());
    }

    public static CompletableFuture<String> $Async (final String requestCommand,
                                                    CurlArgumentsBuilder.CurlJavaOptions curlJavaOptions) throws CurlException {
        return Curl.curlAsync (requestCommand, curlJavaOptions).thenApply ((httpResponse) -> IOUtils.quietToString (httpResponse.getEntity ()));
    }

    public static CurlArgumentsBuilder curl () {
        return new CurlArgumentsBuilder ();
    }

    public static CompletableFuture<HttpResponse> curlAsync (final String requestCommand) throws CurlException {
        return curlAsync (requestCommand, with ().build ());
    }

    public static CompletableFuture<HttpResponse> curlAsync (final String requestCommand,
                                                             CurlArgumentsBuilder.CurlJavaOptions curlJavaOptions) throws CurlException {
        return CompletableFuture.supplyAsync (() -> {
            try {
                return Curl.curl (requestCommand, curlJavaOptions);
            } catch (IllegalArgumentException e) {
                throw new CurlException (e);
            }
        }).toCompletableFuture ();
    }

    public static HttpResponse curl (final String requestCommand) throws CurlException {
        return curl (requestCommand, with ().build ());
    }

    public static HttpResponse curl (final String requestCommand,
                                     CurlArgumentsBuilder.CurlJavaOptions curlJavaOptions) throws CurlException {
        try {
            final CommandLine commandLine = ReadArguments.getCommandLineFromRequest (requestCommand,
                    curlJavaOptions.getPlaceHolders ());
            stopAndDisplayVersionIfThe (commandLine.hasOption (Arguments.VERSION.getOpt ()));
            final HttpResponse response =
                    HttpClientProvider.prepareHttpClient (commandLine, curlJavaOptions.getInterceptors (),
                            curlJavaOptions.getConnectionManager ()).execute (
                            HttpRequestProvider.prepareRequest (commandLine));
            AfterResponse.handle (commandLine, response);
            return response;
        } catch (final IOException | IllegalArgumentException e) {
            throw new CurlException (e);
        }
    }

    public static String getVersion () {
        return Version.NUMBER;
    }

    public static String getVersionWithBuildTime () {
        return Version.NUMBER + " (Build time : " + Version.BUILD_TIME + ")";
    }

    public static class CurlArgumentsBuilder {

        private final StringBuilder curlCommand = new StringBuilder ("curl ");
        private CurlJavaOptions curlJavaOptions = with ().build ();

        public static class CurlJavaOptions {
            private final List<BiFunction<HttpRequest, Supplier<HttpResponse>, HttpResponse>> interceptors;
            private final List<String> placeHolders;
            private final HttpClientConnectionManager connectionManager;

            private CurlJavaOptions (Builder builder) {
                interceptors = builder.interceptors;
                placeHolders = builder.placeHolders;
                connectionManager = builder.connectionManager;
            }

            public static Builder with () {
                return new Builder ();
            }

            public List<BiFunction<HttpRequest, Supplier<HttpResponse>, HttpResponse>> getInterceptors () {
                return interceptors;
            }

            public List<String> getPlaceHolders () {
                return placeHolders;
            }

            public HttpClientConnectionManager getConnectionManager () {
                return connectionManager;
            }

            public static final class Builder {
                private List<BiFunction<HttpRequest, Supplier<HttpResponse>, HttpResponse>> interceptors
                        = new ArrayList<> ();
                private List<String> placeHolders;
                private HttpClientConnectionManager connectionManager;

                private Builder () {
                }

                public Builder interceptor (BiFunction<HttpRequest, Supplier<HttpResponse>, HttpResponse> val) {
                    interceptors.add (val);
                    return this;
                }

                public Builder placeHolders (List<String> val) {
                    placeHolders = val;
                    return this;
                }

                public Builder connectionManager (HttpClientConnectionManager val) {
                    connectionManager = val;
                    return this;
                }

                public CurlJavaOptions build () {
                    return new CurlJavaOptions (this);
                }
            }
        }

        CurlArgumentsBuilder () {
        }

        public CurlArgumentsBuilder javaOptions (CurlJavaOptions curlJavaOptions) {
            this.curlJavaOptions = curlJavaOptions;
            return this;
        }

        public String $ (final String url) throws CurlException {
            this.curlCommand.append (url).append (" ");
            return Curl.$ (this.curlCommand.toString (), curlJavaOptions);
        }

        public CompletableFuture<String> $Async (final String url) throws CurlException {
            this.curlCommand.append (url).append (" ");
            return Curl.$Async (this.curlCommand.toString (), curlJavaOptions);
        }

        public HttpResponse run (final String url) throws CurlException {
            this.curlCommand.append (url).append (" ");
            return Curl.curl (this.curlCommand.toString (), curlJavaOptions);
        }

        public CompletableFuture<HttpResponse> runAsync (final String url) throws CurlException {
            this.curlCommand.append (url).append (" ");
            return Curl.curlAsync (this.curlCommand.toString (), curlJavaOptions);
        }

    }

    public static class CurlException extends RuntimeException {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        CurlException (final Throwable arg0) {
            super (arg0);
        }
    }

    public static void main(String[] args)
    {
    	// let's instantiate a curl object and feed it whatever parameter we can find.
    	String fullargs="";
    	for(String arg:args)
    	{
    		fullargs+=arg+" ";
    	}
    	// Now that I have all the arguments, run it.
    	System.out.println($(fullargs));
    }
}
