BEGIN TRANSACTION;
CREATE TABLE "users" (
	`username`	TEXT NOT NULL,
	`password`	TEXT NOT NULL,
	`server`	TEXT NOT NULL DEFAULT '127.0.0.1',
	`smtp`	INTEGER NOT NULL DEFAULT 465,
	`imap`	INTEGER NOT NULL DEFAULT 993,
	`key`	INTEGER NOT NULL DEFAULT 'foobar',
	PRIMARY KEY(`username`)
);
INSERT INTO `users` VALUES ('ebuill@adept.com','foobar','138.197.104.156',465,993,'foobar');
CREATE TABLE "mailboxes" (
	`username`	TEXT NOT NULL,
	`mailbox`	TEXT NOT NULL,
	PRIMARY KEY(`username`,`mailbox`),
	FOREIGN KEY(`username`) REFERENCES users(username)
);
CREATE TABLE "emails" (
	`email_id`	INTEGER NOT NULL,
	`username`	TEXT NOT NULL,
	`mailbox`	TEXT NOT NULL,
	`to`	TEXT NOT NULL,
	`from`	TEXT NOT NULL,
	`subject`	TEXT NOT NULL,
	`body`	TEXT NOT NULL,
	`read`	TEXT NOT NULL,
	PRIMARY KEY(`email_id`),
	FOREIGN KEY(`username`) REFERENCES `user`(`username`),
	FOREIGN KEY(`mailbox`) REFERENCES `mailboxes`(`mailbox`)
);
COMMIT;
