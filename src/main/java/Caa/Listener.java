package Caa;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class Listener extends ListenerAdapter {
	
	public static String prefix = "%";
	public String fundsMessage;
	EmbedBuilder embedBuilder;
	
	public Listener() {
		embedBuilder = new EmbedBuilder();
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] arg = event.getMessage().getContentRaw().split("\\s+");
		if(!event.getAuthor().isBot() && !event.getAuthor().isSystem()) {
			if (arg[0].equalsIgnoreCase(prefix + "setup") && event.getMember().isOwner()) {
				if(arg.length < 2) {
					errorMessage(event.getChannel(),"Please Enter the Channel to Setup");
					return;
				}
				switch(arg[1]) {	
				case "accounting":
					System.out.println("accounting channel id: "+event.getChannel().getIdLong());
					event.getMessage().delete().queue( message ->{
						Main.accountingChannelId = event.getChannel().getIdLong();
						successMessage(event.getChannel(),"Accounting Channel Set to: "+event.getChannel().getAsMention());
						Main.updateStatus();
					});
					break;					
				case "bank":
					System.out.println("bank channel id: "+event.getChannel().getIdLong());
					if(Main.bankChannelId == 0) {
						Main.bankChannelId = event.getChannel().getIdLong();
						Main.originFundsMessageId = event.getChannel().getHistory().retrievePast(2).complete().get(1).getIdLong();
						fundsMessage = event.getChannel().retrieveMessageById(Main.originFundsMessageId).complete().getContentRaw();
						event.getChannel().sendMessage("Please Wait...").queue(message -> {
							Main.fundsMessageId = message.getIdLong();
							System.out.println("funds message id: " + Main.fundsMessageId);
							updateFundsMessage();
						});
						event.getMessage().delete().queue();
						event.getChannel().deleteMessageById(Main.originFundsMessageId).queue();
						parseFundsMessage();
						Main.updateStatus();
					}else {
						Main.jda.getTextChannelById(Main.bankChannelId).deleteMessageById(Main.fundsMessageId).queue();
						Main.bankChannelId = event.getChannel().getIdLong();
						event.getChannel().sendMessage("Please Wait...").queue(message -> {
							Main.fundsMessageId = message.getIdLong();
							updateFundsMessage();
						});
						event.getMessage().delete().queue();
					}
					break;		
					
				default:
					errorMessage(event.getChannel(),"Please Enter the Channel to Setup");
					break;
				}
				
			}
			else if (arg[0].equalsIgnoreCase(prefix + "off") && event.getMember().isOwner()) {
				event.getMessage().delete().queue(message -> {
					event.getJDA().shutdown();
				});
			}
			if(Main.accountingChannelId != 0 && Main.bankChannelId != 0) {
				if(event.getChannel().getIdLong() == Main.accountingChannelId) {
		
					if (arg[0].charAt(0) == '+'|| arg[0].charAt(0) == '-') {
						try {
							Float funds = Main.accounts.get(arg[1].replace("!","")) + Float.parseFloat(arg[0].replace("$",""));
							Main.accounts.put(arg[1].replace("!",""),funds);
							try {
								if(arg[0].charAt(0) == '-') {
									Float royalties =Main.accounts.get("<@433074544534618112>") + (Float.parseFloat(arg[0].replace("$",""))* -0.05f);
									Main.accounts.get(arg[1].replace("!","")) - Float.parseFloat(arg[0].replace("$","") * -0.05f);
									Main.accounts.put("<@433074544534618112>",royalties);
								}else {
									Float royalties = Main.accounts.get("<@433074544534618112>") + (Float.parseFloat(arg[0].replace("$",""))* 0.05f);
									Main.accounts.get(arg[1].replace("!","")) - Float.parseFloat(arg[0].replace("$","") * 0.05f);
									Main.accounts.put("<@433074544534618112>",royalties);
								}
							}catch(Exception e) {
								Main.accounts.put("<@433074544534618112>",15.00f);
								if(arg[0].charAt(0) == '-') {
									Float royalties = Main.accounts.get("<@433074544534618112>") + (Float.parseFloat(arg[0].replace("$",""))* -0.05f);
									Main.accounts.get(arg[1].replace("!","")) - Float.parseFloat(arg[0].replace("$","") * -0.05f);
									Main.accounts.put("<@433074544534618112>",royalties);
								}else {
									Float royalties = Main.accounts.get("<@433074544534618112>") + (Float.parseFloat(arg[0].replace("$",""))* 0.05f);
									Main.accounts.get(arg[1].replace("!","")) - Float.parseFloat(arg[0].replace("$","") * 0.05f);
									Main.accounts.put("<@433074544534618112>",royalties);
								}
							}
							updateFundsMessage();
						}catch(Exception e) {
							event.getMessage().delete().queueAfter(5000,TimeUnit.MILLISECONDS);
							errorMessage(event.getChannel(),"Format Error: Transaction Must Be Structured as Follows:\n\n +/-[AMOUNT] [ACCOUNT] \n\n e.g.:\n\n +$10 @Person");
						}
					}  else if (arg[0].equalsIgnoreCase(prefix + "newaccount")) {
						if(arg.length < 2) {
							errorMessage(event.getChannel(),"Please Enter a Valid Name for the New Account");
						}else if(Main.accounts.containsKey(arg[1].replace("!",""))){
							errorMessage(event.getChannel(),"Account Already Exists");
						}else if(arg.length > 2 || !(arg[1].contains("<@") && arg[1].contains(">"))){
							errorMessage(event.getChannel(),"Account Name Must Be a Single Mention");
						}else {
							Main.accounts.put(arg[1].replace("!",""),15.00f);
							successMessage(event.getChannel(),"Account Created");
							updateFundsMessage();
						}
						event.getMessage().delete().queue();
					}else if (arg[0].equalsIgnoreCase(prefix + "removeaccount")) {
						if(arg.length < 2) {
							errorMessage(event.getChannel(),"Please Enter a Valid Name for the Account");
						}else if(arg.length > 2 || !(arg[1].contains("<@") && arg[1].contains(">"))){
							errorMessage(event.getChannel(),"Account Name Must Be a Single Mention");
						}else if(arg[1].replace("!","").contentEquals("<@433074544534618112>") ){
							errorMessage(event.getChannel(),"Don't do that.");
						}else if(!Main.accounts.containsKey(arg[1].replace("!",""))){
							errorMessage(event.getChannel(),"Account Does Not Exists");
						}else {
							Main.accounts.remove(arg[1].replace("!",""));
							successMessage(event.getChannel(),"Account Removed");
							updateFundsMessage();
						}
							event.getMessage().delete().queue();
					}else if (arg[0].equalsIgnoreCase(prefix + "tax") && event.getMember().isOwner()) {
						tax();
						updateFundsMessage();
					}else if (arg[0].equalsIgnoreCase(prefix + "transfer")) {
						if(arg.length >= 4) {
							try {
								Float funds = Float.parseFloat(arg[1].replace("$",""));
								Main.accounts.put(arg[2].replace("!",""),(Main.accounts.get(arg[2].replace("!","")) - funds));
								Main.accounts.put(arg[3].replace("!",""),(Main.accounts.get(arg[3].replace("!","")) + funds - funds * 0.05f));
								Float royalties = Main.accounts.get("<@433074544534618112>") + funds * 0.05f;
								Main.accounts.put("<@433074544534618112>",royalties); 
								updateFundsMessage();
							}catch(Exception e) {
								e.printStackTrace();
								errorMessage(event.getChannel(),"Format Error: Transfer Must Be Structured as Follows:\n\n %transfer [AMOUNT] [SENDER] [RECIPIENT] \n\n e.g.:\n\n %transfer $10 @person1 @person2");
							}
						}else {
							errorMessage(event.getChannel(),"Format Error: Transfer Must Be Structured as Follows:\n\n %transfer [AMOUNT] [SENDER] [RECIPIENT] \n\n e.g.:\n\n %transfer $10 @person1 @person2");
							event.getMessage().delete().queueAfter(5000,TimeUnit.MILLISECONDS);
						}
					}
					else{
						event.getMessage().delete().queue();
						errorMessage(event.getChannel(),event.getChannel().getAsMention() + " is only for Accounting");
					}
				}
			}
		}
	}
	
	private void tax() {
		float tax = 0;
		for (String key : Main.accounts.keySet()) {
			if(key != "<@302951942294863872>" && Main.accounts.get(key) > 0) {
				tax += Main.accounts.get(key) * 0.145f;
				Main.accounts.put(key,(Main.accounts.get(key) - Main.accounts.get(key)*0.15f));
			}
		}
		Main.accounts.put("<@302951942294863872>",(Main.accounts.get("<@302951942294863872>") + tax));
	}
	
	private void updateFundsMessage(){
		fundsMessage = "";
		sortHashMap();
		for (String key : Main.accounts.keySet()) {
			fundsMessage = fundsMessage+key+"   "+"$"+ String.valueOf(Main.accounts.get(key))+"\n";
		}
		Main.jda.getTextChannelById(Main.bankChannelId).editMessageById(Main.fundsMessageId,"~ TOTAL COMPANY FUNDS: $" +getTotalFunds()+" ~" +"\n\n" + fundsMessage).queue();
	}
	
	private float getTotalFunds() {
		float funds = 0;
		for (String key : Main.accounts.keySet()) {
			if(Main.accounts.get(key) > 0) {
				funds +=  Main.accounts.get(key);
			}
		}
		return funds;
	}
	
	private void sortHashMap() {
		Main.accounts = (LinkedHashMap<String, Float>) MapUtil.sortByValue(Main.accounts);
	}
	
	private void errorMessage(TextChannel channel,String errorMessage) {
		embedBuilder.setTitle("Error");
		embedBuilder.setColor(0xdb3327);
		embedBuilder.setDescription(errorMessage);
		channel.sendMessage(embedBuilder.build()).queue(message -> {
			message.delete().queueAfter(5000,TimeUnit.MILLISECONDS);
		});
	}
	
	private void successMessage(TextChannel channel,String confirmation) {
		embedBuilder.setTitle("Success");
		embedBuilder.setColor(0x42ed5c);
		embedBuilder.setDescription(confirmation);
		channel.sendMessage(embedBuilder.build()).queue(message -> {
			message.delete().queueAfter(3000,TimeUnit.MILLISECONDS);
		});
	}
	
	
	private void parseFundsMessage() {
		String[] accountsSplit = fundsMessage.split("\n");
		for(int i = 0; i < accountsSplit.length; i++ ) {
			String[] temp = accountsSplit[i].split("\\s+");
			Main.accounts.put(temp[0].replace("!",""), Float.parseFloat(temp[1].replace("$","").replace(",","")));
		}
	}
}
