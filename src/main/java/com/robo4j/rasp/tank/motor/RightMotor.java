/*
 * Copyright (C) 2016-2017, Miroslav Wengner, Marcus Hirt
 * This RightMotor.java  is part of robo4j.
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

package com.robo4j.rasp.tank.motor;

import com.robo4j.commons.annotation.RoboMotor;
import com.robo4j.rpi.motor.RpiBaseMotor;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 17.12.2016
 */
@RoboMotor(id = RightMotor.MOTOR_NAME)
public class RightMotor extends RpiBaseMotor {

    private static final int ARDUINO_ADDRESS = 0x04;
    static final String MOTOR_NAME = "right";

    public RightMotor() {
        super(ARDUINO_ADDRESS, (byte)2, 20);
        this.port = 2;
        this.speed = 40;
    }


}
