package samurai;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

/**
 * Main Class
 * Initializes Samurai bot
 */
public class Bot {

    public static final String AVATAR = "https://cdn.discordapp.com/avatars/270044218167132170/c3b45c87f7b63e7634665a11475beedb.jpg";
    private static final String TOKEN = "MjcwMDQ0MjE4MTY3MTMyMTcw.C1yJ0Q.oyQMo7ZGXdaq2K3P43NMwOO8diM";
    public static User self;

    public static void main(String[] args) {

        try {
            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
            SamuraiListener listener = new SamuraiListener();
            JDA jda = jdaBuilder
                    .addListener(listener)
                    .setToken(TOKEN)
                    .buildBlocking();
            listener.setJDA(jda);
            self = jda.getSelfUser();
        } catch (LoginException | RateLimitedException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
