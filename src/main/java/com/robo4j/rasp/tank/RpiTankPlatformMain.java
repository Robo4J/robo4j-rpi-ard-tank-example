/*
 * Copyright (C) 2016-2017, Miroslav Wengner, Marcus Hirt
 * This RpiTankPlatformMain.java  is part of robo4j.
 * module: robo4j-rpi-ard-tank-example
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.rasp.tank;

import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.registry.RoboRegistry;
import com.robo4j.core.Robo4jBrick;
import com.robo4j.core.client.enums.RequestStatusEnum;
import com.robo4j.core.client.request.RequestProcessorCallable;
import com.robo4j.core.client.request.RequestProcessorFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 18.12.2016
 */
public class RpiTankPlatformMain {

    private static final int PORT = 8025;

    public static void main(String[] args) {
        SimpleLoggingUtil.debug(RpiTankPlatformMain.class, "TANK START");
        new RpiTankPlatformMain();
    }

    @SuppressWarnings(value = "unchecked")
    public RpiTankPlatformMain() {
        SimpleLoggingUtil.print(RpiTankPlatformMain.class, "SERVER starts...");
        Robo4jBrick robo4jBrick = new Robo4jBrick(getClass(), false);
        final RoboRegistry<RoboRegistry, RoboSystemConfig> systemServiceRegistry
                = robo4jBrick.getRegistryByType(RegistryTypeEnum.SERVICES);
        SimpleLoggingUtil.debug(getClass(), "systemServiceRegistry: " + systemServiceRegistry.getRegistry()
                .entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));


        robo4jBrick.activateEngineRegistry();
        final AtomicBoolean active = new AtomicBoolean(true);

        robo4jBrick.submit(new RequestProcessorCallable(null));

        try(ServerSocket server = new ServerSocket(PORT)){

            final RequestProcessorFactory factory = RequestProcessorFactory.getInstance();

            while(active.get()){
                Socket request = server.accept();
                Future<RequestStatusEnum> result = robo4jBrick.submit(new RequestProcessorCallable(request));
                switch (result.get()){
                    case ACTIVE:
                        break;
                    case NONE:
                        break;
                    case EXIT:
                        SimpleLoggingUtil.debug(getClass(), "IS EXIT: " + result);
                        active.set(false);
                        break;
                    default:
                        break;
                }
            }
        } catch (InterruptedException | ExecutionException | IOException e){
            SimpleLoggingUtil.print(RpiTankPlatformMain.class, "SERVER CLOSED");
        }

        robo4jBrick.end();
        SimpleLoggingUtil.print(RpiTankPlatformMain.class, "FINAL END");
        System.exit(0);
    }
}
