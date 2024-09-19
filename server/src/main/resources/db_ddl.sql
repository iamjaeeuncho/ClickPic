DROP TABLE "USERS" CASCADE CONSTRAINTS;
DROP TABLE "COMMENTS" CASCADE CONSTRAINTS;
DROP TABLE "FRAMES" CASCADE CONSTRAINTS;
DROP TABLE "LIKES" CASCADE CONSTRAINTS;
DROP TABLE "PHOTOFINAL" CASCADE CONSTRAINTS;
DROP TABLE "PHOTOROOMS" CASCADE CONSTRAINTS;
DROP TABLE "ROOMPARTICIPANTS" CASCADE CONSTRAINTS;


CREATE TABLE Users (
    user_id NUMBER PRIMARY KEY,
    nickname VARCHAR(100) NOT NULL,
    status CHAR(1) DEFAULT 'Y' NOT NULL,
    created_at TIMESTAMP NOT NULL
);

COMMENT ON COLUMN Users.status IS 'Y: È¸¿ø / N:Å»Åð';

CREATE TABLE Rooms (
    room_id NUMBER PRIMARY KEY,
    frame_id VARCHAR2(255) NOT NULL,
    user_id NUMBER NOT NULL,
    user_count NUMBER NOT NULL,
    room_session VARCHAR2(255) NOT NULL,
    room_name VARCHAR2(255) NOT NULL,
    origin_photo VARCHAR2(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_photoro FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE Participants (
    user_id NUMBER NOT NULL,
    room_id NUMBER NOT NULL,
    PRIMARY KEY (user_id, room_id),
    CONSTRAINT fk_user_participants FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_room_participants FOREIGN KEY (room_id) REFERENCES PhotoRooms(room_id)
);

CREATE TABLE Frames (
    frame_id VARCHAR2(255) PRIMARY KEY,
    frame_url VARCHAR(255) NOT NULL
);

CREATE TABLE Likes (
    user_id NUMBER NOT NULL,
    photo_id NUMBER NOT NULL,
    CONSTRAINT fk_user_likes FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_photo_likes FOREIGN KEY (photo_id) REFERENCES PhotoFinal(photo_id)
);

CREATE TABLE Photos (
    photo_id NUMBER PRIMARY KEY,
    room_id NUMBER NOT NULL,
    origin_photo VARCHAR(255) NOT NULL,
    complete_photo VARCHAR(255) NOT NULL,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL,
    is_public VARCHAR(5) NULL,
    note VARCHAR(255) NULL,
    CONSTRAINT fk_room_photofinal FOREIGN KEY (room_id) REFERENCES PhotoRooms(room_id)
);

COMMENT ON COLUMN PhotoFinal.is_public IS 't: °ø°³ / f: ºñ°ø°³';

CREATE TABLE Comments (
    comment_id VARCHAR(255) PRIMARY KEY,
    user_id NUMBER NOT NULL,
    photo_id NUMBER NULL,
    content VARCHAR(255) NOT NULL,
    created_at DATE NOT NULL,
    CONSTRAINT fk_user_comments FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_photo_comments FOREIGN KEY (photo_id) REFERENCES PhotoFinal(photo_id)
);

---------------------

CREATE SEQUENCE ROOM_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE PHOTO_ID_SEQ START WITH 1 INCREMENT BY 1;
