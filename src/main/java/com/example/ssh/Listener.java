package com.example.ssh;

import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.channel.ChannelListener;
import org.apache.sshd.common.session.Session;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Listener implements ChannelListener {

    static final @NotNull Logger logger = LoggerFactory.getLogger("com.example.ssh.Channel");

    @Override
    public void channelInitialized(@NotNull Channel channel) {
        long channelId = channel.getChannelId();
        Session session = channel.getSession();
        String user = session == null? "null": session.getUsername();
        String remote = session == null ? "null" : session.getRemoteAddress().toString();
        logger.info("channelInitialized channel: {}, user: {}, remote: {}", channelId, user, remote);
    }

    @Override
    public void channelOpenSuccess(@NotNull Channel channel) {
        long channelId = channel.getChannelId();
        Session session = channel.getSession();
        String user = session == null? "null": session.getUsername();
        String remote = session == null ? "null" : session.getRemoteAddress().toString();
        logger.info("channelOpenSuccess channel: {}, user: {}, remote: {}", channelId, user, remote);
    }

    @Override
    public void channelOpenFailure(@NotNull Channel channel, @NotNull Throwable reason) {
        long channelId = channel.getChannelId();
        Session session = channel.getSession();
        String user = session == null? "null": session.getUsername();
        String remote = session == null ? "null" : session.getRemoteAddress().toString();
        logger.info("channelOpenFailure channel: {}, user: {}, remote: {} caused by[{}, {}]", channelId, user, remote, reason.getClass(), reason.toString());
    }

    @Override
    public void channelStateChanged(@NotNull Channel channel, String hint) {
        long channelId = channel.getChannelId();
        Session session = channel.getSession();
        String user = session == null? "null": session.getUsername();
        String remote = session == null ? "null" : session.getRemoteAddress().toString();
        logger.info("channelStateChanged channel: {}, user: {}, remote: {}, hint: {}", channelId, user, remote, hint);
    }

    @Override
    public void channelClosed(@NotNull Channel channel, @NotNull Throwable reason) {
        long channelId = channel.getChannelId();
        Session session = channel.getSession();
        String user = session == null? "null": session.getUsername();
        String remote = session == null ? "null" : session.getRemoteAddress().toString();
        logger.info("channelClosed channel: {}, user: {}, remote: {} caused by[{}, {}]", channelId, user, remote, reason.getClass(), reason.toString());
    }
}
