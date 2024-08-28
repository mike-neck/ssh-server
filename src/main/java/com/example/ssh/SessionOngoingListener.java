package com.example.ssh;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class SessionOngoingListener implements SessionListener {

    static final @NotNull Logger logger = LoggerFactory.getLogger("com.example.ssh.OnSession");

    @Override
    public void sessionEvent(@NotNull Session session, Event event) {
        String user = session.getUsername();
        SocketAddress remote = session.getRemoteAddress();
        logger.info("[event] user: {}, remote: {}, event: {}", user, remote, event);
    }

    @Override
    public void sessionException(@NotNull Session session, @NotNull Throwable t) {
        String user = session.getUsername();
        SocketAddress remote = session.getRemoteAddress();
        logger.info("[exception] user: {}, remote: {}, caused by: {} {}", user, remote, t.getClass(), t.getMessage());
    }
}
