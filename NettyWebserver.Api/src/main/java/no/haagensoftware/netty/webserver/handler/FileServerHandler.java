package no.haagensoftware.netty.webserver.handler;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import org.apache.log4j.Logger;
import org.haagensoftware.netty.webserver.util.ContentTypeUtil;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.*;
import java.util.Set;

/**
 * A file server that can serve files from file system and class path.
 *
 * If you wish to customize the error message, please sub-class and override sendError().
 * Based on Trustin Lee's original file serving example
 */
public class FileServerHandler extends SimpleChannelUpstreamHandler {
	private static Logger logger = Logger.getLogger(FileServerHandler.class.getName());
	
    private String rootPath;
    private String stripFromUri;
    private int cacheMaxAge = -1;
    private boolean fromClasspath = false;
    private MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    public FileServerHandler(String path) {
        if (path.startsWith("classpath://")) {
            fromClasspath = true;
            //rootPath = getClass().getResource(path.replace("classpath://", "")).getPath();
            rootPath = path.replace("classpath://", "");
            if (rootPath.lastIndexOf("/") == rootPath.length() -1)
                rootPath = rootPath.substring(0, rootPath.length() -1);
        } else {
            rootPath = path;
        }
        rootPath = rootPath.replace(File.separatorChar, '/');
    }

    public FileServerHandler(String path, String stripFromUri) {
        this(path);
        this.stripFromUri = stripFromUri;
    }

    public FileServerHandler(String path, int cacheMaxAge) {
        this(path);
        this.cacheMaxAge = cacheMaxAge;
    }

    public FileServerHandler(String path, int cacheMaxAge, String stripFromUri) {
        this(path, cacheMaxAge);
        this.stripFromUri = stripFromUri;
    }

    public MimetypesFileTypeMap getFileTypeMap() {
		return fileTypeMap;
	}
    
    public int getCacheMaxAge() {
		return cacheMaxAge;
	}
    
    public String getRootPath() {
		return rootPath;
	}
    
    public boolean isPut(MessageEvent e) {
    	HttpRequest request = (HttpRequest) e.getMessage();
		HttpMethod method = request.getMethod();
		return method == HttpMethod.PUT;
    }
    
    public boolean isPost(MessageEvent e) {
    	HttpRequest request = (HttpRequest) e.getMessage();
		HttpMethod method = request.getMethod();
		return method == HttpMethod.POST;
    }
    
    public boolean isGet(MessageEvent e) {
    	HttpRequest request = (HttpRequest) e.getMessage();
		HttpMethod method = request.getMethod();
		return method == HttpMethod.GET;
    }
    
    public boolean isDelete(MessageEvent e) {
    	HttpRequest request = (HttpRequest) e.getMessage();
		HttpMethod method = request.getMethod();
		return method == HttpMethod.DELETE;
    }
    
    public String getCookieValue(MessageEvent e, String cookieName) {
    	String cookieValue = null;
    	
    	HttpRequest request = (HttpRequest) e.getMessage();
		String value = request.getHeader("Cookie");
		Set<Cookie> cookies = new CookieDecoder().decode(value);
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(cookieName)) {
				cookieValue = cookie.getValue();
				break;
			}
		}
    	
    	return cookieValue;
    }
    
    public String getHttpMessageContent(MessageEvent e) {
		String requestContent = null;
		HttpRequest request = (HttpRequest) e.getMessage();
		ChannelBuffer content = request.getContent();
        if (content.readable()) {
        	requestContent = content.toString(CharsetUtil.UTF_8);
        }
		return requestContent;
	}
    
    public void writeContentsToBuffer(ChannelHandlerContext ctx, String responseText, String contentType) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setHeader(CONTENT_TYPE, "text/json; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(responseText + "\r\n", CharsetUtil.UTF_8));
        
        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}
    
    @Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
        if (request.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        String uri = request.getUri();
        
        final String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }


        ChannelBuffer content = getFileContent(path);
        if (content == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        String contentType = ContentTypeUtil.getContentType(path);
        logger.info("contentType: " + contentType + " for path: " + path);
        
        DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);;
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
        setContentLength(response, content.readableBytes());

        response.setContent(content);
        ChannelFuture writeFuture = e.getChannel().write(response);

        // Decide whether to close the connection or not.
        if (!isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
	}

    protected ChannelBuffer getFileContent(String path) {
        InputStream is;
        try {
            if (fromClasspath) {
                is = this.getClass().getResourceAsStream(rootPath + path);
            } else {
                is = new FileInputStream(rootPath + "/" + path);
            }

            if (is == null) {
                return null;
            }
            
            final int maxSize = 512 * 1024;
            ByteArrayOutputStream out = new ByteArrayOutputStream(maxSize);
            byte[] bytes = new byte[maxSize];

            while (true) {
                int r = is.read(bytes);
                if (r == -1) break;
                
                out.write(bytes, 0, r);
            }

            ChannelBuffer cb = ChannelBuffers.copiedBuffer(out.toByteArray());
            out.close();
            is.close();
            return cb;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    protected String sanitizeUri(String uri) throws URISyntaxException {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        // Convert file separators.
        uri = uri.replace(File.separatorChar, '/');

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + ".") ||
            uri.contains("." + File.separator) ||
            uri.startsWith(".") || uri.endsWith(".")) {
            return null;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        uri = decoder.getPath();
    	
        if (uri.endsWith("/")) {
            uri += "index.html";
        }

        if (uri.startsWith("/attachment")) {
        	uri = uri.substring(11);
        }
        
        logger.info(uri);
        
        return uri;
    }

    protected void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
