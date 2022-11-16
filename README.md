# CaptCommunity Assistent V2
**CapCom Assistent** is a small project for a single Discord Community Server. This is the second interration , supporting simple multithreading (incl. scheduling), database pooling & newer Interaction Events.

> **Info:** This Bot contain hard coded content and most likely does not work on your own server, if you run it yourself!

> This is an example on how u could program diverse features with the [JDA](https://github.com/DV8FromTheWorld/JDA) (needlessly to say, not the most optimal way)
***
## Features
* **Server Managment**
	* Auto Roler
	* Join/Leave/Ban/Unban Announcer
	* Copy/Clear Message History
	* Logging to local files

* **Fun Commands**
	* Role Dice
	* Coin flip
	* Blackjack
	* Lootbox and gambling
	* Warframe API Tracker

* **Music**
	* Player (Message binded) **.1**
	* DB Playlist
	* Simple Web service (unstable/WIP)

* **Others**
	* Static DB Connection (with Pooling) **.2**
	* Temp Channel
	* Caching (Message & Channel IDs, Webhooks) **.3**
	* AssetPool (Images (local or Link))
>**.1** The Player supports Twitch, Soundcloud & Youtube. It use the [Walkyst Lavaplayer-fork](https://github.com/Walkyst/lavaplayer-fork)<br>
>**.2** The DB Connection does not use Prepared Statementsn this example, which risk a injection<br>
>**.3** Values can be declared in the outer properties files
***
## Dependencies

> **JDK:** 1.8 / 11, **Maven Framework**
* [JDA](https://github.com/DV8FromTheWorld/JDA) by DV8FromTheWorld
* [Discord Webhooks](https://github.com/MinnDevelopment/discord-webhooks) by MinnDevelopment
* [Lavaplayer-fork](https://github.com/Walkyst/lavaplayer-fork) by Walkyst (original. sedmelluq)
* [Log4j core](https://github.com/apache/logging-log4j2) by Apache
* [slf4j API](https://mvnrepository.com/artifact/org.slf4j/slf4j-api) & [slf4j simple](https://mvnrepository.com/artifact/org.slf4j/slf4j-simple) by slf4j
* [jcabi-log](https://github.com/jcabi/jcabi-log) by jcabi
* [json](https://github.com/stleary/JSON-java) by stleary
* [MySQL Connection](https://mvnrepository.com/artifact/mysql/mysql-connector-java) by MySQL
* [cron4j](https://mvnrepository.com/artifact/it.sauronsoftware.cron4j/cron4j/2.2.5) by sauronsoftware

## Visual Examples
**WIP**
