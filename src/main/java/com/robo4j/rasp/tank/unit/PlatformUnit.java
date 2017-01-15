/*
 * Copyright (C) 2016-2017, Miroslav Wengner, Marcus Hirt
 * This PlatformUnit.java  is part of robo4j.
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

package com.robo4j.rasp.tank.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ProcessAgent;
import com.robo4j.commons.agent.ProcessAgentBuilder;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.command.RoboUnitCommand;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.motor.GenericMotor;
import com.robo4j.commons.registry.EngineRegistry;
import com.robo4j.commons.unit.DefaultUnit;
import com.robo4j.core.client.enums.RequestCommandEnum;
import com.robo4j.core.platform.ClientPlatformException;
import com.robo4j.rasp.tank.platform.ClientPlatformConsumer;
import com.robo4j.rasp.tank.platform.ClientPlatformProducer;
import com.robo4j.rpi.unit.RpiUnit;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 17.12.2016
 */

@RoboUnit(id = PlatformUnit.UNIT_NAME, system = PlatformUnit.SYSTEM_NAME, producer = PlatformUnit.PRODUCER_NAME, consumer = {
		"left", "right" })
public class PlatformUnit extends DefaultUnit implements RpiUnit {

	private static final int AGENT_PLATFORM_POSITION = 0;
	private static final String[] CONSUMER_NAME = { "left", "right" };
	static final String UNIT_NAME = "platformUnit";
	static final String SYSTEM_NAME = "tankBrick1";
	static final String PRODUCER_NAME = "default";

	private volatile LinkedBlockingQueue<GenericCommand<RequestCommandEnum>> commandQueue;

	public PlatformUnit() {
		SimpleLoggingUtil.debug(getClass(), "PlatformUnit");
	}

	@Override
	public void setExecutor(final ExecutorService executor) {
		this.executorForAgents = executor;
	}

	@Override
	protected GenericAgent createAgent(String name, AgentProducer producer, AgentConsumer consumer) {
		return Objects.nonNull(producer) && Objects.nonNull(consumer)
				? ProcessAgentBuilder.Builder(executorForAgents).setProducer(producer).setConsumer(consumer).build()
				: null;
	}

	@Override
	public Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> initLogic() {
		return null;
	}

	@Override
	public boolean isActive() {
		return active.get();
	}

	// TODO: looks like similar to all
	@Override
	public RpiUnit init(Object input) {
		if (Objects.nonNull(executorForAgents)) {
			this.agents = new ArrayList<>();
			this.active = new AtomicBoolean(false);
			this.commandQueue = new LinkedBlockingQueue<>();
			SimpleLoggingUtil.print(PlatformUnit.class, "TankRpi: INIT");
			final Exchanger<GenericCommand<RequestCommandEnum>> platformExchanger = new Exchanger<>();

			final Map<String, GenericMotor> enginesMap = EngineRegistry.getInstance().getByNames(CONSUMER_NAME);

			this.agents.add(createAgent("platformAgent", new ClientPlatformProducer(commandQueue, platformExchanger),
					new ClientPlatformConsumer(executorForAgents, platformExchanger, enginesMap)));

			if (!agents.isEmpty()) {
				active.set(true);
				logic = initLogic();
			}
		}

		return this;
	}

	@SuppressWarnings(value = "unchecked")
	@Override
	public boolean process(RoboUnitCommand command) {
		try {
			GenericCommand<RequestCommandEnum> processCommand = (GenericCommand<RequestCommandEnum>) command;
			SimpleLoggingUtil.debug(getClass(), "Tank Command: " + command);
			commandQueue.put(processCommand);
			ProcessAgent platformAgent = (ProcessAgent) agents.get(AGENT_PLATFORM_POSITION);
			platformAgent.setActive(true);
			platformAgent.getExecutor().execute((Runnable) platformAgent.getProducer());
			final Future<Boolean> engineActive = platformAgent.getExecutor()
					.submit((Callable<Boolean>) platformAgent.getConsumer());
			try {
				platformAgent.setActive(engineActive.get());
			} catch (InterruptedException | ConcurrentModificationException | ExecutionException e) {
				throw new ClientPlatformException("SOMETHING ERROR CYCLE COMMAND= ", e);
			}
			return true;

		} catch (InterruptedException e) {
			throw new ClientPlatformException("PLATFORM COMMAND e= ", e);
		}
	}

	@Override
	public String getUnitName() {
		return UNIT_NAME;
	}

	@Override
	public String getSystemName() {
		return SYSTEM_NAME;
	}

	@Override
	public String[] getProducerName() {
		return new String[] { PRODUCER_NAME };
	}

	@Override
	public String getConsumerName() {
		return Arrays.asList(CONSUMER_NAME).toString();
	}

}
