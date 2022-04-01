/*
    Copyright 2021 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.model.events;

import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * Event for notifying state changes for commands
 */
public class CommandEvent implements UGSEvent {
    private final CommandEventType commandEventType;
    private final GcodeCommand command;

    public CommandEvent(CommandEventType commandEventType, GcodeCommand command) {
        this.commandEventType = commandEventType;
        this.command = command;
    }

    public CommandEventType getCommandEventType() {
        return commandEventType;
    }

    public GcodeCommand getCommand() {
        return command;
    }
}
