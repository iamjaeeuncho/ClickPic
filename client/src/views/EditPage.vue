<template>
  <div class="main-container">
    <div class="largeTitle">DECORATING ROOM</div>
    <div id="drawing-picture" class="drawing-container">
      <div class="canvas-container">
        <canvas ref="canvas"></canvas>
      </div>
      <div class="right-panel">
        <p class="middleTitle">꾸미기 순서</p>
        <div v-if="isMyTurn" class="output">{{ localUserNickname }}님의 차례입니다</div>
        <div v-else class="output">{{ currentTurnNickname }}님의 차례입니다</div>
        <p class="middleTitle">펜 색상</p>
        <div class="color-tools">
          <div v-for="(color, index) in colors" :key="index" :style="{ backgroundColor: color }" class="color-field" @click="changeColor(color)"></div>
        </div>
        <div class="color-tools">
          <input type="color" v-model="drawColor" class="color-picker" />
          <input type="range" v-model="drawWidth" min="1" max="100" class="pen-range" />
        </div>
        <p class="middleTitle">스티커</p>
        <div class="stickers">
          <img v-for="(sticker, index) in stickers" :key="index" :src="sticker" draggable="true" @dragstart="dragStart" class="sticker-image" />
        </div>
        <button @click="clearCanvas" class="btn-rounded">초기화</button>
        <button @click="undoLast" class="btn-rounded">되돌리기</button>
        <button v-if="isMyTurn && !isLastTurn" @click="passTurn" class="btn-rounded">순서 넘기기</button>
      </div>
    </div>
    <div class="center">
      <button v-if="isMyTurn && isLastTurn" class="btn-large" @click="saveCanvas">사진 전송하기</button>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import alien from "@/assets/sticker/alien.png";
import butterfly from "@/assets/sticker/butterfly.png";
import fireworks from "@/assets/sticker/fireworks.png";
import havefun from "@/assets/sticker/havefun.png";
import heart from "@/assets/sticker/heart.png";
import rainbow from "@/assets/sticker/rainbow.png";
import ribbon from "@/assets/sticker/ribbon.png";
import smile from "@/assets/sticker/smile.png";
import star from "@/assets/sticker/star.png";
import sunglass from "@/assets/sticker/sunglass.png";

export default {
  name: 'EditPage',
  props: ['roomSession'],
  data() {
    return {
      canvasWidth: this.$route.query.canvasWidth || 600,
      canvasHeight: this.$route.query.canvasHeight || 800,
      currentDrawing: [],
      drawingActions: [],
      stickerActions: [],
      undoStack: [],
      redoStack: [],
      canvas: null,
      context: null,
      drawColor: "#000000",
      drawWidth: 5,
      isDrawing: false,
      lastX: 0,
      lastY: 0,
      colors: ["#FF0000", "#FFFF00", "#0000FF", "#FF00FF", "#FFFFFF", "#000000"],
      stickers: [alien, butterfly, havefun, smile, heart, fireworks, rainbow, ribbon, star, sunglass],
      draggingSticker: null,
      roomInfo: {},
      webSocket: null,
      isLastTurn: false,
      isMyTurn: false,
      currentTurnNickname: "",
      localUserNickname: '',
      restoreArray: [],
      index: -1,
      isHost: this.$route.params.isHost,
      userActions: {} // 사용자별 작업 기록 저장 객체
    };
  },
  created() {
    const storedNickname = localStorage.getItem('nickname');
    if (storedNickname) {
      this.localUserNickname = storedNickname;
      console.log('User Nickname from Local Storage: ', this.localUserNickname);
    } else {
      console.error('No userNickname found in local storage!');
    }

    this.isHost = this.$route.query.isHost === 'true';
    this.fetchRoomInfo().then(() => {
      this.initializeWebSocket();
    });
  },
  methods: {
    async fetchRoomInfo() {
      try {
        const response = await axios.get(`${process.env.VUE_APP_BACKEND_URL}/photo/info`, {
          params: { roomSession: this.roomSession }
        });
        this.roomInfo = response.data;

        if (this.roomInfo.originPhoto) {
          this.initializeCanvas();
        } else {
          console.error('Origin photo not found in roomInfo');
        }
      } catch (error) {
        console.error('Error fetching room info:', error);
      }
    },
    initializeWebSocket() {
      if (!this.localUserNickname) {
        console.error('Cannot establish WebSocket connection: userNickname is undefined.');
        return;
      }

      this.webSocket = new WebSocket(`wss://4cutstudio.store/ws/turn?nickname=${encodeURIComponent(this.localUserNickname)}`);

      this.webSocket.onmessage = (message) => {
        const data = JSON.parse(message.data);

        if (data.type === 'SHOW_ALERT') {
            alert("그림을 그려주세요!");  // 자동으로 알림 표시
    }
        if (data.type === 'SESSION_ID') {
          this.userId = data.userId;
          console.log('Session ID received: ', this.userId);
        }
        if (data.type === 'UNDO_LAST' && data.userId !== this.userId) {
          this.restoreCanvas();
        }
        if (data.type === 'CLEAR_CANVAS' && data.userId !== this.userId) {
          this.restoreCanvas();
        }
        if (data.type === 'DRAW') {
          this.context.strokeStyle = data.color;
          this.context.lineWidth = data.width;
          this.context.beginPath();
          this.context.moveTo(data.fromX, data.fromY);
          this.context.lineTo(data.toX, data.toY);
          this.context.stroke();
          this.context.closePath();
        }

        if (data.type === 'STICKER') {
          const sticker = new Image();
          sticker.src = data.stickerSrc;
          sticker.onload = () => {
            this.context.drawImage(sticker, data.x, data.y, 100, 100);
          };
        }

        if (data.type === 'PHOTO_SAVED') {
          if (this.isHost) {
            this.$router.push({
              name: 'Save',
              params: {
                roomSession: this.roomSession,
                imageUrl: encodeURIComponent(data.imageUrl),
                userId: this.userId,
                isHost: this.isHost,
                sessionId: this.userId
              }
            }).catch(err => {
              if (err.name !== 'NavigationDuplicated') {
                console.error(err);
              }
            });
          } else {
            alert('사진이 완성되었습니다');
            this.$router.push({ name: 'Home' });
          }
        }

        if (data.type === 'TURN') {
          console.log("유저 턴 확인 data.userId:",data.userId)
          console.log("유저 턴 확인 data.userId:",this.userId)
          this.$set(this, 'isMyTurn', data.userId === this.userId);
          this.currentTurnNickname = data.nickname;
          this.isLastTurn = data.isLastTurn;

        } else if (data.type === 'ERROR') {
          alert(data.message);
        }
      };
    },

    clearCanvas() {
      if (!this.isMyTurn) {
        alert('It is not your turn!');
        return;
      }

      // 현재 사용자 작업 초기화
      this.userActions[this.userId] = [];

      // 캔버스를 지우고 원래 이미지를 복원
      this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
      const image = new Image();
      image.crossOrigin = "Anonymous";
      image.src = this.roomInfo.originPhoto;
      image.onload = () => {
        this.context.drawImage(image, 0, 0, this.canvas.width, this.canvas.height);
        this.restoreOtherUsersDrawing(); // 다른 사용자의 작업 복원
      };

      // 서버로 초기화 메시지 전송
      this.webSocket.send(JSON.stringify({ type: 'CLEAR_CANVAS', userId: this.userId }));
    },

    undoLast() {
      if (!this.isMyTurn) {
        alert('It is not your turn!');
        return;
      }

      const lastAction = this.undoStack.pop();

      if (!lastAction) {
        alert('되돌릴 작업이 없습니다.');
        return;
      }

      if (lastAction.userId === this.userId) {
        if (lastAction.type === 'DRAW') {
          this.drawingActions = this.drawingActions.filter(action => !lastAction.actions.includes(action));
          this.restoreCanvas();
        }

        if (lastAction.type === 'STICKER') {
          this.stickerActions = this.stickerActions.filter(action => action !== lastAction);
          this.restoreCanvas();
        }

        this.webSocket.send(JSON.stringify({ type: 'UNDO_LAST', userId: this.userId }));
      }
    },

    passTurn() {
      if (this.isMyTurn) {
        this.saveCurrentCanvas();
        this.webSocket.send(JSON.stringify({ type: 'PASS_TURN' }));
        this.isMyTurn = false;
      } else {
        alert('당신의 턴이 아닙니다!');
      }
    },

    saveCurrentCanvas() {
      const currentCanvasData = this.canvas.toDataURL();
      this.fixedDrawing = currentCanvasData;
    },

    restoreCanvas() {
      const img = new Image();
      img.src = this.fixedDrawing;
      img.onload = () => {
        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.context.drawImage(img, 0, 0);
      };
    },

    restoreOtherUsersDrawing() {
      Object.keys(this.userActions).forEach(userId => {
        if (userId !== this.userId) {
          this.userActions[userId].forEach(action => {
            if (action.type === 'DRAW') {
              this.context.strokeStyle = action.color;
              this.context.lineWidth = action.width;
              this.context.beginPath();
              this.context.moveTo(action.fromX, action.fromY);
              this.context.lineTo(action.toX, action.toY);
              this.context.stroke();
            } else if (action.type === 'STICKER') {
              const sticker = new Image();
              sticker.src = action.src;
              sticker.onload = () => {
                this.context.drawImage(sticker, action.x, action.y, 100, 100);
              };
            }
          });
        }
      });
    },

    initializeCanvas() {
      if (!this.roomInfo.originPhoto) {
        return;
      }

      this.canvas = this.$refs.canvas;
      this.context = this.canvas.getContext("2d");
      this.canvas.width = this.canvasWidth;
      this.canvas.height = this.canvasHeight;
      console.log("width" + this.canvas.width);
      console.log("height" + this.canvas.height);

      const image = new Image();
      image.crossOrigin = "Anonymous";
      image.src = this.roomInfo.originPhoto;

      image.onload = () => {
        this.context.drawImage(image, 0, 0, this.canvas.width, this.canvas.height);
      };

      this.canvas.addEventListener("mousedown", this.startDrawing.bind(this));
      this.canvas.addEventListener("mousemove", this.draw.bind(this));
      this.canvas.addEventListener("mouseup", this.stopDrawing.bind(this));
      this.canvas.addEventListener("mouseout", this.stopDrawing);
      this.canvas.addEventListener("touchstart", this.startDrawing.bind(this));
      this.canvas.addEventListener("touchmove", this.draw.bind(this));
      this.canvas.addEventListener("touchend", this.stopDrawing);

      this.canvas.addEventListener("dragover", this.allowDrop);
      this.canvas.addEventListener("drop", this.dropSticker);
    },

    fixDrawing() {
      if (!this.userActions[this.userId]) {
        this.$set(this.userActions, this.userId, []);
      }
      this.userActions[this.userId].push(...this.currentDrawing);
      this.currentDrawing = [];
    },

    changeColor(color) {
      this.drawColor = color;
    },

    startDrawing(event) {
      if (!this.isMyTurn) {
        alert('It is not your turn!');
        return;
      }
      this.isDrawing = true;
      this.lastX = event.clientX - this.canvas.offsetLeft;
      this.lastY = event.clientY - this.canvas.offsetTop;
      this.currentDrawing = [];
      event.preventDefault();
    },

    draw(event) {
      if (!this.isDrawing) return;

      const currentX = event.clientX - this.canvas.offsetLeft;
      const currentY = event.clientY - this.canvas.offsetTop;

      this.context.strokeStyle = this.drawColor;
      this.context.lineWidth = this.drawWidth;
      this.context.beginPath();
      this.context.moveTo(this.lastX, this.lastY);
      this.context.lineTo(currentX, currentY);
      this.context.stroke();
      this.context.closePath();

      this.currentDrawing.push({
        type: 'DRAW',
        userId: this.userId,
        color: this.drawColor,
        width: this.drawWidth,
        fromX: this.lastX,
        fromY: this.lastY,
        toX: currentX,
        toY: currentY
      });

      this.webSocket.send(JSON.stringify({
        type: 'DRAW',
        color: this.drawColor,
        width: this.drawWidth,
        fromX: this.lastX,
        fromY: this.lastY,
        toX: currentX,
        toY: currentY
      }));

      this.lastX = currentX;
      this.lastY = currentY;
    },

    stopDrawing(event) {
      if (!this.isDrawing) return;
      this.isDrawing = false;

      if (this.currentDrawing.length > 0) {
        this.undoStack.push({
          type: 'DRAW',
          userId: this.userId,
          actions: this.currentDrawing
        });
      }

      event.preventDefault();
    },

    async saveCanvas() {
      const imageBase64 = this.canvas.toDataURL('image/png');
      const photoData = {
        originPhoto: imageBase64,
        roomId: this.roomInfo.roomId
      };

      try {
        const response = await axios.post(`${process.env.VUE_APP_BACKEND_URL}/photo/complete?roomSession=${this.roomSession}`, photoData, {
          headers: { 'Content-Type': 'application/json' }
        });

        this.webSocket.send(JSON.stringify({
          type: 'PHOTO_SAVED',
          imageUrl: response.data
        }));
      } catch (error) {
        console.error('Error uploading image to server:', error.response ? error.response.data : error.message);
      }
    },

    dragStart(event) {
      this.draggingSticker = event.target;
    },

    allowDrop(event) {
      event.preventDefault();
    },

    dropSticker(event) {
      if (!this.isMyTurn) {
        alert('It is not your turn!');
        return;
      }

      event.preventDefault();

      const x = event.offsetX;
      const y = event.offsetY;
      const sticker = new Image();
      sticker.src = this.draggingSticker.src;

      sticker.onload = () => {
        this.context.drawImage(sticker, x, y, 100, 100);

        this.stickerActions.push({ type: 'STICKER', userId: this.userId, src: sticker.src, x, y });
        this.undoStack.push({ type: 'STICKER', userId: this.userId });

        this.webSocket.send(JSON.stringify({
          type: 'STICKER',
          userId: this.userId,
          stickerSrc: sticker.src,
          x: x,
          y: y,
        }));
      };
    }
  }
};
</script>

<style scoped>
.main-container {
  position: relative;
  width: 100%;
  height: auto;
  display: flex;
  flex-direction: column;
}

.drawing-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  flex-grow: 1;
  margin-top: 20px;
}

.canvas-container {
  margin-right: 180px;
  margin-top: 30px;
}

canvas {
  position: relative;
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
}

.right-panel {
    width: 100%;
    max-width: 300px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.color-tools {
  display: flex;
  width: 100%;
  gap: 10px;
  align-items: center;
}

.color-field {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
}

.color-picker {
  width: 50px;
  height: 40px;
}

.pen-range {
  width: 150px;
}

.stickers {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sticker-image {
  width: 50px;
  height: 50px;
  cursor: move;
}

</style>