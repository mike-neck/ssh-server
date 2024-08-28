package com.example.ssh;

import org.apache.sshd.common.forward.TcpipForwardingExceptionMarker;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.session.ServerSessionImpl;
import org.apache.sshd.server.session.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshSessions extends SessionFactory {

    private static final @NotNull Logger logger = LoggerFactory.getLogger(SshSessions.class);

    public SshSessions(@NotNull SshServer server) {
        super(server);
    }

    @Override
    protected ServerSessionImpl doCreateSession(IoSession ioSession) throws Exception {
        return super.doCreateSession(ioSession);
    }

    @Override
    public void exceptionCaught(@NotNull IoSession ioSession, Throwable cause) {
        ioSession.setAttribute(TcpipForwardingExceptionMarker.class, cause);
        log.warn("exceptionCaught({}) {}: {}", ioSession, cause.getClass().getSimpleName(), cause.getMessage(), cause);
    }
}
