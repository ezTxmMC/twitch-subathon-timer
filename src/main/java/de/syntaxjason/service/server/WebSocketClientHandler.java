package de.syntaxjason.service.server;

import com.google.gson.Gson;
import de.syntaxjason.model.ServerMessage;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.function.Consumer;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketClientHandshaker handshaker;
    private final Consumer<ServerMessage> messageHandler;
    private final Consumer<Boolean> connectionHandler;
    private final Gson gson;
    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, Consumer<ServerMessage> messageHandler, Consumer<Boolean> connectionHandler, Gson gson) {
        this.handshaker = handshaker;
        this.messageHandler = messageHandler;
        this.connectionHandler = connectionHandler;
        this.gson = gson;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected");
        connectionHandler.accept(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {
            handleHandshake(ctx, msg);
            return;
        }

        if (msg instanceof FullHttpResponse) {
            handleHttpResponse((FullHttpResponse) msg);
            return;
        }

        if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHandshake(ChannelHandlerContext ctx, Object msg) {
        try {
            handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
            System.out.println("WebSocket Client connected");
            handshakeFuture.setSuccess();
            connectionHandler.accept(true);
        } catch (WebSocketHandshakeException e) {
            System.err.println("WebSocket Client failed to connect");
            handshakeFuture.setFailure(e);
            connectionHandler.accept(false);
        }
    }

    private void handleHttpResponse(FullHttpResponse response) {
        throw new IllegalStateException(
                "Unexpected FullHttpResponse (getStatus=" + response.status() +
                        ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            handleTextFrame((TextWebSocketFrame) frame);
            return;
        }

        if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
            return;
        }

        if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received close frame");
            ctx.channel().close();
        }
    }

    private void handleTextFrame(TextWebSocketFrame frame) {
        String text = frame.text();

        try {
            ServerMessage message = gson.fromJson(text, ServerMessage.class);
            messageHandler.accept(message);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
        connectionHandler.accept(false);
    }
}
