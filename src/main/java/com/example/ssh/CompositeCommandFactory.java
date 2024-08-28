package com.example.ssh;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CompositeCommandFactory implements CommandFactory {

    static final Logger logger = LoggerFactory.getLogger("com.example.ssh.CommandFactory");

    final List<CommandFactory> delegates;

    public CompositeCommandFactory(CommandFactory @NotNull ... delegates) {
        this(List.of(delegates));
    }

    public CompositeCommandFactory(List<CommandFactory> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Command createCommand(ChannelSession channel, String command) throws IOException {
        IOException exception = null;
        for (CommandFactory factory : delegates) {
            try {
                return factory.createCommand(channel, command);
            } catch (IOException e) {
                ServerSession session = channel.getServerSession();
                String  user = session != null? session.getUsername(): "unknown session user";
                logger.info("[create-command] factory {} not supported command: '{}' by [{}]", factory, command, user);
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        logger.info("none of command cannot support command {}", command);
        if (exception == null) {
            throw new IOException("no factory found");
        } else {
            throw exception;
        }
    }
}
