create table if not exists afk
(
    userID  varchar(50)   null,
    message varchar(1500) null
);

create table if not exists autorole
(
    roleID  varchar(50) null,
    guildID varchar(50) null
);

create table if not exists dmmessages
(
    message varchar(50) null,
    guildID varchar(50) null
);

create table if not exists interactionrole
(
    channelID varchar(50) null,
    guildID   varchar(50) null,
    messageID varchar(50) null
);

create table if not exists interactionrole_buttons
(
    guildID   varchar(50)  null,
    messageID varchar(50)  null,
    buttonID  varchar(100) null,
    roleID    varchar(50)  null
);

create table if not exists interactionrole_selectmenu
(
    messageID varchar(50)  null,
    guildID   varchar(50)  null,
    choiceID  varchar(150) null,
    roleID    varchar(50)  null
);

create table if not exists joinmessages
(
    message   varchar(5000) null,
    channelID varchar(50)   null,
    guildID   varchar(50)   null
);

create table if not exists lang
(
    lang    char(2)     null,
    guildID varchar(50) null
);

create table if not exists leavemessages
(
    message   varchar(5000) null,
    channelID varchar(50)   null,
    guildID   varchar(50)   null
);

create table if not exists logs
(
    channelCreate             varchar(6) default 'false' null,
    channelDelete             varchar(6) default 'false' null,
    channelNSFWUpdate         varchar(6) default 'false' null,
    channelTopicUpdate        varchar(6) default 'false' null,
    channelNameUpdate         varchar(6) default 'false' null,
    channelSlowmodeUpdate     varchar(6) default 'false' null,
    emojiAdded                varchar(6) default 'false' null,
    emojiRemoved              varchar(6) default 'false' null,
    emojiUpdateName           varchar(6) default 'false' null,
    guildBan                  varchar(6) default 'false' null,
    guildUnban                varchar(6) default 'false' null,
    guildMemberJoin           varchar(6) default 'false' null,
    guildMemberRemove         varchar(6) default 'false' null,
    guildMemberRoleAdd        varchar(6) default 'false' null,
    guildMemberRoleRemove     varchar(6) default 'false' null,
    guildMemberUpdateNickname varchar(6) default 'false' null,
    guildMemberUpdateTimeOut  varchar(6) default 'false' null,
    guildVoiceDeafen          varchar(6) default 'false' null,
    guildVoiceGuildDeafen     varchar(6) default 'false' null,
    guildVoiceMute            varchar(6) default 'false' null,
    guildVoiceGuildMute       varchar(6) default 'false' null,
    guildVoiceJoin            varchar(6) default 'false' null,
    guildVoiceLeave           varchar(6) default 'false' null,
    guildVoiceMove            varchar(6) default 'false' null,
    messageUpdate             varchar(6) default 'false' null,
    messageDelete             varchar(6) default 'false' null,
    messageBulkDelete         varchar(6) default 'false' null,
    roleCreate                varchar(6) default 'false' null,
    roleDelete                varchar(6) default 'false' null,
    roleUpdateColor           varchar(6) default 'false' null,
    roleUpdateHoisted         varchar(6) default 'false' null,
    roleUpdateIcon            varchar(6) default 'false' null,
    roleUpdateMentionable     varchar(6) default 'false' null,
    roleUpdateName            varchar(6) default 'false' null,
    roleUpdatePermissions     varchar(6) default 'false' null,
    roleUpdatePosition        varchar(6) default 'false' null,
    guildID                   varchar(50)                null,
    channelID                 varchar(50)                null
);

create table if not exists reactionRole
(
    messageID varchar(50) null,
    roleID    varchar(50) null,
    emojiID   varchar(50) null
);

create table if not exists remind
(
    userID    varchar(50)  null,
    name      varchar(512) null,
    timestamp bigint       null
);

create table if not exists twitternotifier
(
    channelID      varchar(50)   null,
    guildID        varchar(50)   null,
    twitterAccount varchar(50)   null,
    webhookURL     varchar(1000) null
);

create table if not exists warn_amount
(
    guildID varchar(50)   null,
    userID  varchar(50)   null,
    amount  int default 0 null
);

create table if not exists warn_config
(
    guildID          varchar(50)   null,
    channelID        varchar(50)   null,
    timeout          int default 0 null,
    timeout_duration int default 0 null,
    kick             int default 0 null,
    ban              int default 0 null
);

create table if not exists warn_reasons
(
    guildID   varchar(50)   null,
    userID    varchar(50)   null,
    reason    varchar(1000) null,
    timestamp int           null
);

