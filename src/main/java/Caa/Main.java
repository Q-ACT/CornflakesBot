package Caa;

import java.util.LinkedHashMap;
import java.util.Scanner;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
	
	public static JDA jda;
	public static LinkedHashMap<String,Float> accounts;
	public static long fundsMessageId = 847961451699372072;
	public static long originFundsMessageId;
	public static long accountingChannelId = 842589756566798386;
	public static long bankChannelId = 842583989846802432;
	public static String bankToSetup = "\n Bank Not Set Up";
	public static String accountingToSetup = "Accounting Not Set Up";
	public static String key = System.getenv().get("TOKEN");
	
	public static void main(String[] args) throws LoginException {
		Scanner scanner = new Scanner(System.in);
		accounts = new LinkedHashMap<>();
		jda = JDABuilder.createDefault(key,GatewayIntent.GUILD_MEMBERS,GatewayIntent.GUILD_MESSAGES).build();
		jda.getPresence().setActivity(Activity.watching(bankToSetup +" || "+ accountingToSetup));
		jda.addEventListener(new Listener());
		while(true) {
			try {	
				String consoleInput = scanner.nextLine();
				switch(consoleInput.split("\\s+")[0]) {
					case "SetupBank":
						bankChannelId= Long.parseLong(consoleInput.split("\\s+")[1]);
						fundsMessageId= Long.parseLong(consoleInput.split("\\s+")[2]);
						try {
							parseFundsMessage();
							updateStatus();
						}catch(Exception e) {
							System.out.println("SETUP FAILED, UNKNOWN ID");
						}
						break;
					case "SetupAccounting":
						accountingChannelId= Long.parseLong(consoleInput.split("\\s+")[1]);
						updateStatus();
						break;
					case "Shutdown":
						scanner.close();
						jda.shutdown();
						return;
					default:
						System.out.println("UNKNOWN COMMAND, COMMAND LIST: \n\n SetupBank [BANK_ID] [BANK_MESSAGE_ID] \n SetupAccounting [ACCOUNTING_CHANNEL_ID] \n Shutdown");
						break;
					}
			} catch(Exception e) {
				System.out.println("UNKNOWN COMMAND, COMMAND LIST: \n\n SetupBank [BANK_ID] [BANK_MESSAGE_ID] \n SetupAccounting [ACCOUNTING_CHANNEL_ID] \n Shutdown");
			}
		}
		
	}
	
	public static void updateStatus() {
		if(bankChannelId == 0) {
			jda.getPresence().setActivity(Activity.watching(bankToSetup));
		}
		else if(accountingChannelId == 0) {
			jda.getPresence().setActivity(Activity.watching(accountingToSetup));
		}
		else {
			jda.getPresence().setActivity(Activity.streaming("Cornflakification","https://www.youtube.com/watch?v=lzSF34-LJV8"));
		}
	}
	private static void parseFundsMessage() {
			String[] accountsSplit = jda.getTextChannelById(bankChannelId).retrieveMessageById(fundsMessageId).complete().getContentRaw().split("\n");
			for(int i = 0; i < accountsSplit.length; i++ ) {
				String[] temp = accountsSplit[i].split("\\s+");
				Main.accounts.put(temp[0].replace("!",""), Float.parseFloat(temp[1].replace("$","").replace(",","")));
			}
		}
}
