package com.tisawesomeness.minecord.command.admin;

import com.tisawesomeness.minecord.command.Command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ShutdownCommand extends Command {
	
	public CommandInfo getInfo() {
		return new CommandInfo(
			"shutdown",
			"Shuts down the bot.",
			null,
			null,
			0,
			true,
			true,
			false
		);
	}
	
	public Result run(String[] args, MessageReceivedEvent e) {
		e.getJDA().shutdown();
		System.exit(0);
		return new Result(Outcome.SUCCESS, "");
	}
	
}
