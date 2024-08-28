package com.example.ssh;

import org.apache.sshd.common.config.keys.IdentityUtils;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionStartEndListener implements SessionListener {

    static final @NotNull Logger logger = LoggerFactory.getLogger("com.example.ssh.Session");

    @Override
    public void sessionEstablished(@NotNull Session session) {
        logger.info("[establish] status: {}, user: {} remote: {}",
                session.getKexState(),
                session.getUsername(),
                session.getRemoteAddress()
        );
    }

    @Override
    public void sessionDisconnect(@NotNull Session session, int reason, String msg, String language, boolean initiator) {
        logger.info("[disconnect] user: {} remote: {} reason: {}, language: {}, message: {} by initiator: {}", session.getUsername(), session.getRemoteAddress(), reason, language, msg, initiator);
    }

    @Override
    public void sessionClosed(Session session) {
        logger.info("[closed] status: {}, user: {} remote: {}",
                session.getKexState(),
                session.getUsername(),
                session.getRemoteAddress()
        );
    }
}
