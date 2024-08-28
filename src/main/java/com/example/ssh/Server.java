package com.example.ssh;

import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.io.nio2.Nio2ServiceFactoryFactory;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.common.util.security.bouncycastle.BouncyCastleSecurityProviderRegistrar;
import org.apache.sshd.common.util.security.eddsa.EdDSASecurityProviderRegistrar;
import org.apache.sshd.common.util.threads.CloseableExecutorService;
import org.apache.sshd.common.util.threads.SshThreadPoolExecutor;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.session.SessionFactory;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.server.shell.UnknownCommandFactory;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Server implements AutoCloseable {

    public static final String SSH_PORT = "SSH_PORT";
    public static final String SSH_AUTHORIZED_KEYS = "SSH_AUTHORIZED_KEYS";
    public static final String SSH_USERNAME = "SSH_USERNAME";
    public static final String SSH_HOST_KEY = "SSH_HOST_KEY";

    final @NotNull SshServer sshServer;

    static final @NotNull Logger logger = LoggerFactory.getLogger(Server.class);

    public Server(@NotNull SshServer sshServer) {
        this.sshServer = sshServer;
    }

    private void configure(int sshPort, HostKeys sshHostKey, CloseableExecutorService exec, CloseableExecutorService ioExec) {
        sshServer.setPort(sshPort);
        PubKey.Collection pubKeys = PubKey.collection(Server.SSH_USERNAME, Server.SSH_AUTHORIZED_KEYS);
        sshServer.setPublickeyAuthenticator(pubKeys);
        sshServer.setShellFactory(new ProcessShellFactory("/usr/bin/bash", "-i", "-l"));
        sshServer.setKeyPairProvider(sshHostKey);
        sshServer.setCommandFactory(new CompositeCommandFactory(ProcessShellCommandFactory.INSTANCE, UnknownCommandFactory.INSTANCE));
        sshServer.setSubsystemFactories(List.of(sftpServer(exec)));
        sshServer.setIoServiceFactoryFactory(new Nio2ServiceFactoryFactory(() -> ioExec));
        sshServer.addSessionListener(new SessionStartEndListener());
        sshServer.addChannelListener(new Listener());
        sshServer.setSessionFactory(new SshSessions(sshServer));
    }

    public static void main(String[] args) throws Exception {
        SecurityUtils.registerSecurityProvider(new BouncyCastleSecurityProviderRegistrar());
        SecurityUtils.registerSecurityProvider(new EdDSASecurityProviderRegistrar());

        int sshPort = Env.notNullInt(SSH_PORT, 2020);
        HostKeys sshHostKey = HostKeys.get(SSH_HOST_KEY);
        if (sshHostKey == null) {
            throw new IllegalStateException("host key not found, please create key(ed25519) and set its directory to env[SSH_HOST_KEY]");
        }
        try (
                CloseableExecutorService exec = new SshThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
                CloseableExecutorService ioExec = new SshThreadPoolExecutor(4, 4, 60 * 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
                SshServer sshServer = SshServer.setUpDefaultServer();
                Server server = new Server(sshServer)
        ) {
            server.configure(sshPort, sshHostKey, exec, ioExec);
            CountDownLatch latch = new CountDownLatch(1);
            sshServer.addCloseFutureListener(future -> {
                Object id = future.getId();
                logger.info("operationComplete({})", id);
                latch.countDown();
            });

            logger.info("starting server on port {}", sshPort);
            sshServer.start();
            latch.await();
            logger.info("application finish");
        }
    }

    static @NotNull SubsystemFactory sftpServer(@NotNull CloseableExecutorService exec) {
        SftpSubsystemFactory.Builder sb = new SftpSubsystemFactory.Builder();
        sb.withExecutorServiceProvider(() -> exec);
        return sb.build();
    }

    @Override
    public void close() throws Exception {
        Iterable<AutoCloseable> closeables = List.of(
                sshServer::stop,
                sshServer
        );
        Exception exception = null;
        for (AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                if (exception == null) {
                    exception = new IllegalStateException("failed to shutdown server");
                }
                exception.addSuppressed(e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
