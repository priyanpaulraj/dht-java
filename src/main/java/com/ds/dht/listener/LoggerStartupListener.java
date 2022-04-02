package com.ds.dht.listener;

import java.util.Enumeration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private boolean started = false;

    private String serverPort = null;

    @Override
    public void start() {

        if (serverPort == null) {
            Enumeration<Object> e = System.getProperties().elements();
            while (e.hasMoreElements()) {
                String arg = e.nextElement().toString();
                if (arg.contains("server.port")) {
                    String[] args = arg.split(" ");
                    for (String a : args) {
                        if (a.contains("server.port")) {
                            serverPort = a.split("=")[1];
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (!started) {
            Context context = getContext();
            context.putProperty("server_port", serverPort);
            started = true;
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
    }

    @Override
    public void onReset(LoggerContext context) {
    }

    @Override
    public void onStop(LoggerContext context) {
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
    }
}
