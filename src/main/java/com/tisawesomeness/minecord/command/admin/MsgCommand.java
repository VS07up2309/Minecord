package com.tisawesomeness.minecord.command.admin;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.command.CommandContext;
import com.tisawesomeness.minecord.util.DiscordUtils;
import com.tisawesomeness.minecord.util.MessageUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class MsgCommand extends Command {
	
	public CommandInfo getInfo() {
		return new CommandInfo(
			"msg",
			"Open the DMs.",
			"<mention|id> <message>",
			new String[]{
				"dm",
				"tell",
				"pm"},
			0,
			true,
			true,
			false
		);
	}
	
	public Result run(CommandContext txt) {
		String[] args = txt.args;
		
		//Check for proper argument length
		if (args.length < 2) {
			return new Result(Outcome.WARNING, ":warning: Please specify a message.");
		}
		
		//Extract user
		User user = DiscordUtils.findUser(args[0], txt.bot.getShardManager());
		if (user == null) return new Result(Outcome.ERROR, ":x: Not a valid user!");
		
		//Send the message
		String msg = null;
		try {
			PrivateChannel channel = user.openPrivateChannel().submit().get();
			msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			channel.sendMessage(msg).queue();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace();
			return new Result(Outcome.ERROR, ":x: An exception occured.");
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		User a = txt.e.getAuthor();
		eb.setAuthor(a.getAsTag() + " (`" + a.getId() + "`)", null, a.getAvatarUrl());
		eb.setDescription("**Sent a DM to " + user.getAsTag() + " (`" + user.getId() + "`):**\n" + msg);
		eb.setThumbnail(user.getAvatarUrl());
		MessageUtils.log(eb.build());
		
		return new Result(Outcome.SUCCESS);
	}
	
}