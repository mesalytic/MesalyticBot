# Mesalytic (Helixus JDA)

![GitHub](https://img.shields.io/github/license/chocololat/Mesalytic)
![Made In Java 18](https://img.shields.io/badge/Made%20in-Java%2018-brightgreen)
![Gradle](https://img.shields.io/badge/Build%20With-Maven-blue)

Mesalytic is the new version of the Helixus Discord bot, written in Java.

It is still in active development, with a release ETA of late october 2022.

## SelfHost

### Disclaimer

A prepared JAR is available at download in the Release tab.

If you want to modify the source code itself to make it fit your needs:

- You are required to have at least some Java knowledge, know how MariaDB databases work, and how Maven works.
- No support will be given for self-host issues, you should know what you are doing before starting.

### Requirements

You need to have the Oracle JDK (or the OpenJDK) in version 18 or later.

You also need to have a MariaDB/MySQL database set up. See [here](#database-schemas) for the schemas.

- If you want to use Music Functionalities, you should need a Lavalink Instance, see [Third Party](#third-party).
- If you want to use the Twitter Notifier feature, you need to have a Twitter Consumer Key/Secret, and a Access Token/Secret. See [this example](https://github.com/chocololat/twitter-oauth-example) to know how to get the Access keys.


### Database Schemas

In order for the bot to properly store user and server settings, we use the MariaDB database.

The database itself contains tables, that are essentials for the bot to properly run.

The database schemas can be found [here](https://github.com/chocololat/Mesalytic/blob/master/database/schemas.sql).

### Environements Variables

There should be a `.env` file present on the root of the directory alongside the JAR file.

Here are the variables that should be set :

- **TOKEN** : The token of the bot.
- **TOKENBETA** (**OPTIONAL**): The token of the bot (when ran with `-Ddev=true` flag)
- **LAVALINKURI** (**OPTIONAL**) : The URI of the Lavalink instance. See [here](https://github.com/freyacodes/Lavalink) for more info
- **LAVALINKPWD** (**OPTIONAL**) : The password of the Lavalink instance. See [here](https://github.com/freyacodes/Lavalink) for more info
- **TWITTER_CONSUMER_KEY** (**OPTIONAL**): The Consumer Key of [your Twitter App](https://developer.twitter.com/en/portal/dashboard).
- **TWITTER_CONSUMER_SECRET** (**OPTIONAL**): The Consumer Secret of [your Twitter App](https://developer.twitter.com/en/portal/dashboard).
- **TWITTER_ACCESS_TOKEN** (**OPTIONAL**): The Access Token of a Twitter Account connected with your Twitter App. See [here](#requirements) for more info.
- **TWITTER_ACCESS_SECRET** (**OPTIONAL**): The Access Secret of a Twitter Account connected with your Twitter App. See [here](#requirements) for more info.
- **DISCORD_STATUS_WEBHOOKURL** : The Discord Webhook URL that will report any Gateway Status linked to your bot.
- **DISCORD_ERROR_WEBHOOKURL** : The Discord Webhook URL that will report any errors to a channel.
- **DISCORD_CMD_WEBHOOKURL** : The Discord Webhook URL that will report any commands used. **Must be used for debugging and safety reasons only. Should not be used to store user data.**

## License
Mesalytic is an open-sourced software licensed under the [Apache License 2.0](https://apache.org/licenses/LICENSE-2.0.txt)

## Third Party
Mesalytic relies on the following projects:

### Used code from
| Name                                                                                                                              | License                                                                                         |
|:----------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------|
| [Twemoji.java (SkyBot)](https://github.com/DuncteBot/SkyBot/blob/main/src/main/java/ml/duncte123/skybot/utils/TwemojiParser.java) | [GNU Affero General Public License v3.0](https://github.com/DuncteBot/SkyBot/blob/main/LICENSE) |                                                                     | [Apache License 2.0](https://github.com/chocololat/mesalytic-api/blob/main/LICENSE)                              |

### Services
| Name                                                                             | License                                                                                       |
|:---------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------|
| [Lavalink](https://github.com/freyacodes/Lavalink)                               | [MIT License](https://github.com/freyacodes/Lavalink/blob/master/LICENSE)                     |
| [LavaSrc](https://github.com/TopiSenpai/LavaSrc)                                 | [Apache License 2.0](https://github.com/TopiSenpai/LavaSrc/blob/master/LICENSE)               |

### Third Party Dependencies
| Name                                                                                        | License                                                                                                                           |
|:--------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)                            | [Apache License 2.0](https://github.com/DV8FromTheWorld/JDA/blob/master/LICENSE)                                                  |
| [DotEnv Java](https://github.com/cdimascio/dotenv-java)                                     | [Apache License 2.0](https://github.com/cdimascio/dotenv-java/blob/master/LICENSE)                                                |
| [Lavalink Client (forked from KittyBot-Org)](https://github.com/chocololat/Lavalink-Client) | [MIT License](https://github.com/chocololat/Lavalink-Client/blob/master/LICENSE)                                                  |
| [Reflections](https://github.com/ronmamo/reflections)                                       | [Apache License 2.0](https://github.com/ronmamo/reflections/blob/master/LICENSE-2.0.txt)                                          |
| [MariaDB Connector/J](https://github.com/mariadb-corporation/mariadb-connector-j)           | [GNU Lesser General Public License v2.1](https://github.com/mariadb-corporation/mariadb-connector-j/blob/master/LICENSE)          |
| [Json Simple](https://github.com/fangyidong/json-simple)                                    | [Apache License 2.0](https://github.com/fangyidong/json-simple/blob/master/LICENSE.txt)                                           |
| [Apache Common Lang](https://github.com/apache/commons-lang)                                | [Apache License 2.0](https://github.com/apache/commons-lang/blob/master/LICENSE.txt)                                              |
| [OkHTTP Client 3](https://github.com/square/okhttp)                                         | [Apache License 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt)                                                    |
| [Exp4J](https://github.com/opencollab/jlatexmath)                                           | [Apache License 2.0](https://github.com/fasseg/exp4j/blob/master/LICENSE)                                                         |
| [JLatexMath](https://github.com/fasseg/exp4j)                                               | [Apache License 2.0](https://github.com/opencollab/jlatexmath/blob/master/LICENSE)                                                |
| [Discord Webhooks](https://github.com/MinnDevelopment/discord-webhooks)                     | [Apache License 2.0](https://github.com/MinnDevelopment/discord-webhooks/blob/master/LICENSE)                                     |
| [Logback Classic](https://github.com/qos-ch/logback)                                        | [Eclipse Public License v1.0 & GNU Lesser General Public License v2.1](https://github.com/qos-ch/logback/blob/master/LICENSE.txt) |
| [Twitter4J (Core, Async, Stream, HTTP2 Support)](https://github.com/Twitter4J/Twitter4J)    | [Apache License 2.0](https://github.com/Twitter4J/Twitter4J/blob/main/LICENSE.txt)                                                |
| [HikariCP](https://github.com/brettwooldridge/HikariCP)                                     | [Apache License 2.0](https://github.com/brettwooldridge/HikariCP/blob/dev/LICENSE)                                                |
| [emoji-java](https://github.com/MinnDevelopment/emoji-java)                                 | [MIT License](https://github.com/MinnDevelopment/emoji-java/blob/master/LICENSE.md)                                               |
| [Twitter4J](https://github.com/Twitter4J/Twitter4J)                                         | [Apache License 2.0](https://github.com/Twitter4J/Twitter4J/blob/main/LICENSE.txt)                                                |
